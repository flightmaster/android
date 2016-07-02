#
# ARCINC 424 waypoint/airspace processor
# 
# (c) 2006/2007 Blackhawk Systems Ltd.
#
# Refer to ARINC424 Primer by Blackhawk systems for details on unpacking of
# records
#

#use strict;

%vorTypes = (     
    VT   => "VORTAC",
    VD   => "VOR/DME",
    H    => "NDB",
    HC   => "NDB",
    HO   => "NDB",
    HM   => "NDB",
    HI   => "NDB",
    V    => "VOR",
    " D" => "DME",
    " T" => "TACAN",
    " I" => "LOC"
);

###############################################################################

sub mkJSON {

    # If the name starts with an underscore, then it's deemed to be a string
    # value and will be quoted. Otherwise, no quotes...

    sub mkPair {
        my ($name, $value) = @_;

        #print "$name is a $value\n";

        # if value is an array, then get its expanded elements in JSON. We are
        # assuming that each element of the array is itself another hash
        
        if (ref($value) eq 'ARRAY') {
            my $expansion="";
            my $lastitem=pop(@$value);
            foreach my $item (@$value) {
                $expansion .= mkJSON(%{$item}).", ";
            }
            $expansion .= mkJSON(%{$lastitem});
            return "\"$name\" : [ ".$expansion."]";
        } elsif (ref($value) eq 'HASH') {
            my $expansion="";
            return "\"$name\" : " .mkJSON(%$value);
        }

        if (substr($name,0,1) eq "_") {
            return "\"".substr($name,1)."\" : \"$value\"";
        } else  {
            return "\"$name\" : $value";
        }
    }

    my (%pairs) = @_;
    my @keys = keys(%pairs);
    die "No keys in hash!" if ( scalar(@keys) == 0);

    my $last = pop @keys;
    my $str = "{";
    foreach my $k (@keys) {
        $str .= mkPair($k, $pairs{$k}).", ";
    }
    $str .= mkPair($last, $pairs{$last})."}";
   
    return $str;    
}

###############################################################################

sub DecimalLatLon {

    my ($lat,$lon, $magv) = @_;

    my ($ns, $latd, $latm, $lats, $latds) = unpack("A1 A2 A2 A2 A2", $lat);
    $lat = $latd+$latm/60+$lats/3600+$latds/360000;
    $lat = -$lat if ($ns eq "S");

    my ($ew, $lond, $lonm, $lons, $londs) = unpack("A1 A3 A2 A2 A2", $lon);
    $lon = $lond+$lonm/60+$lons/3600+$londs/360000;
    $lon = -$lon if ($ew eq "W");

    if ($magv) {

        my ($ewm, $mv) = unpack("A1 A4", $magv);
        $magv = $mv/10;
        $magv = -$magv if ($ewm eq "W");

    }

    return ($lat, $lon, $magv);

}

###############################################################################

sub LatLonMV {

    my ($lat, $lon, $magv) = @_;

    sub checkReplace {
        my ($coord) = @_;
        $c=substr($coord,0,1);
        $lead = ($c eq "W" || $c eq "E") ? "" : "0"; 
        $c = ( $c eq "S" || $c eq "W") ? "-" : "+";
        return $c.$lead.substr($coord,1);
    }
        
    if ($magv) {

        my ($ewm, $mv) = unpack("A1 A4", $magv);
        $magv = $mv*10;
        $magv = -$magv if ($ewm eq "W");

    }

    return (checkReplace($lat), checkReplace($lon),$magv);
}

################################################################################

sub Navaid {

    my ($line) = @_;

    my ($subtype, $ident, $id2, $frequency, $aidType, $lat, $lon,$magv, $alt,$name) = 
       unpack("x5 A1 x7 A6 A2 x1 A5 A2 x3 A9 A10 x23 A5 A5 x9 A30" , $line);
    #            6  7  14 20 22 23 28 30 33 42  52  75 80 85 94  
    
    if (!$lat || !$lon) {

        #print "$ident has no lat/lon, skipping\n";
        return;

    }

    #
    # process Lat/Lon into decimal values
    #
    
    ($lat, $lon, $magv) = LatLonMV($lat, $lon, $magv);

    if ($subtype eq "B") {

        $type = "NDB";

    } else {

        $type = $vorTypes{$aidType};

    }

    $uid = "$ident-$id2".substr($line,4,2);
    if (!$type) {

        print "$uid has no type (found $aidType), skipping\n";
        return;

    }

    #
    # reconsider what to do here. Co-located VHF/NDB records
    # exist, perhaps can be merged? NDB/DME and VOR/NDB
    #
    # next if waypoint exists?
    #

    if ($name{$uid}) {

        print "Navaid $uid exists as a $type{$uid} (this record=$type), skipping\n";
        return;

    }

    $identifier{$uid} = $ident;
    $name{$uid} = $name;
    $type{$uid}=$type;
    $lat{$uid} = $lat;
    $lon{$uid} = $lon;
    $magv{$uid} = $magv;
    $alt{$uid} = $alt ? $alt : 0;
    
    $frequency /= $type eq "NDB" ? 10 : 100;
    
    $frequency = sprintf($type eq "NDB" ? "%.1f" : "%.02f", $frequency);

    $notes{$uid} = "Type: $type\nFrequency:\t$frequency";

}

###############################################################################

sub Airfield {

    my ($line) = @_;

    my ($ident, $lat, $lon, $magv, $alt, $name) = 
       unpack("x6 A4 x22 A9 A10 A5 A5 x32 A30", $line);
    #            7  11  33 42  52 57 62  94

    ($lat, $lon, $magv) = LatLonMV($lat, $lon, $magv);

    if ($name{$ident}) {

        print "Airfield $ident exists, skipping\n";
        return;

    }

    $identifier{$ident} = $ident;
    $name{$ident} = $name;
    $type{$ident} = "AIRPORT";
    $lat{$ident} = $lat;
    $lon{$ident} = $lon;
    $magv{$ident} = $magv;
    $alt{$ident} = $alt ? $alt : 0;

    $notes{$ident} = "Type: AIRPORT\n";

    $trafficPatterns{$ident} = "";
    $runways{$ident} = "";
}

###############################################################################

sub Runway {

    my ($line) = @_;

    my ($ident, $runwayMag, $runwaySide, $length, $width) = 
        unpack("x6 A4 x5 A2 A1 x4 A5 x50 A3", $line);
        #         7  11 16 18 19 23 28  78

    $runwayIdent="$ident-$runwayMag$runwaySide";
    $runwayHeading{$runwayIdent} = substr($line, 27,3);

    $line=<FH>;
    my ($surface) = unpack("x23 A4",$line);

    die if ($runwayMag == 0);
    die "Can't attach runway to $ident" if (!$name{$ident});

    $traffic = substr($line,35,1);

    if ($traffic eq "R") {

        $trafficPatterns{$ident} .= ", " if ($trafficPatterns{$ident});
        $trafficPatterns{$ident} .= "$runwayMag$runwaySide";

    }


    return if ($runwayMag > 18);

    $length = int(($length/$runwayFactor)+0.5);
    $width = int(($width/$runwayFactor)+0.5);

    if ($runwaySide ne " ") {

        $runwayOtherside = $runwaySide;
        $runwayOtherside = "R" if ($runwaySide eq "L");
        $runwayOtherside = "L" if ($runwaySide eq "R");

    } else {
    
        $runwaySide = "";
        $runwayOtherside = "";

    }

    $lights = (substr($line,27,1) eq "Y" ? "*" :"").(substr($line,28,1) eq "Y" ? "=":"");
    $lights = " $lights" if ($lights);

    $runways{$ident} = "Runways:\nRunway   LxW       Surface\n" if (!$runways{$ident});

    $runways{$ident} .= pack("A9 A10", "$runwayMag$runwaySide/".($runwayMag+18).$runwayOtherside, 
        "${length}x$width"). "$surface$lights\n";

}
        
###############################################################################


###############################################################################

sub ILS {

    my($line) = @_;

    my ($ident, $ilsIdent, $frequency, $runway) =
        unpack("x6 A4 x3 A3 x6 A5 x2 A3", $line);
        #         7  11 14 17 23 28 30

    $frequency /= 100;

    $runwayIdent="$ident-$runway";
    $ils{$ident} .= "ILS:\nRwy Hdg Freq.\n" if (!$ils{$ident});

    $ils{$ident} .= pack("A5 A5", $runway, $runwayHeading{$runwayIdent})."$frequency / $ilsIdent\n";

}

###############################################################################

sub CommFrequency {

    my ($line) = @_;

    my ($ident, $type, $frequency, $name) =
        unpack("x6 A4 x3 A3 x1 A5 x76 A24",$line);
        #         7  11 14 17 18 23  99

    die "Can't attach frequency to $ident" if (!$name{$ident});

    $frequency /= 100;

    if (!$frequencies{$ident}) {

        $notes{$ident} .= "\nFrequencies:\nType  Freq.   Name\n" ;
        $frequencies{$ident} = "tmp";

    } 

    #
    # discard if type/frequency was just used
    #

    if ($frequencies{$ident} ne "$type$frequency") {

        $frequencies{$ident} = "$type$frequency";
        $notes{$ident} .= pack("A6 A8",$type, sprintf("%03.2f",$frequency))."$name\n";

    }

}

###############################################################################

sub Waypoint {

    my ($line) = @_;

    my ($ident, $id2, $lat, $lon, $magv) =
        unpack("x13 A6 A2 x11 A9 A10 x23 A5",$line);
        #          14 20 22  33 42  52  75

    if (!$lat || !$lon) {

        #print "$ident has no lat/lon, skipping\n";
        return;

    }

    ($lat, $lon, $magv) = LatLonMV($lat, $lon, $magv);

    $type = "WAYPOINT";

    $uid = "$ident-$id2".substr($line,4,2);

    if ($name{$uid}) {

        print "Waypoint $uid exists as $type{$uid}, skipping\n";
        return;

    }

    $identifier{$uid} = $ident;
    $name{$uid} = $ident;
    $type{$uid}=$type;
    $lat{$uid} = $lat;
    $lon{$uid} = $lon;
    $magv{$uid} = $magv;
    $alt{$uid} = 0;
    
    $notes{$uid} = "Type: WAYPOINT\n";

}

###############################################################################

sub AirspaceAltitude {

    #
    # converts from ARINC 424 altitude to FMA
    #

    my ($altitude) = @_;

    if ($altitude =~ /FL([0-9]+)/) {

        $result = "F$1";

    } elsif ($altitude =~ /([0-9]+)M/) {

		$alt = $1 + 0;
        $result = "A$alt";

    } elsif ($altitude =~ /([0-9]+)A/) {

		$alt = $1 + 0;
        $result = "G$alt";

    } elsif ($altitude eq "GND" || $altitude eq "NESTB" || $altitude eq "MSL") {
        
        $result = "G0";
        
    } elsif ($altitude eq "UNLTD" || $altitude eq "NOTSP" || $altitude eq "NOTAM" ) {
        
        $result = F999;

    } elsif ($altitude =~ /([0-9]+)/) {
		$alt = $1+0;
        $result = "A$alt";

    } else {

        die "Protocol error, unable to decode altitude ($altitude)";

    }

    return $result;

}

###############################################################################

sub Airspace {

    my ($line) = @_;

    my ($type, $low, $high, $name) = unpack("x5 A1 x75 A6 A6 A30", $line);

    if ($type eq "C") {

        $class = substr($line, 16,1);
        $class = "G" if ($class eq " ");

        if ($class !~ /[A-G]/) {
            
            print "Skipping Unrecognised airspace class ($name/$class)\n";
            return;

        }

    } else {

        $class = "S".substr($line, 8,1);
        $class = "SR" if ($class !~ /S[ADMPRTW]/);

    }

    $low = AirspaceAltitude($low);
    $high = AirspaceAltitude($high);

    #
    # Get notes/controlling agency
    #

    $line2 = <FH>;
    my ($notes) = unpack("x98 A24", $line2);
    $notes .= "\\n";
    
    #
    # Need to keep track of where we are before reading the next line, as it
    # could be the start of the next record. There is always at least one T or
    # N line though.
    #

    $save = tell FH;

    while (substr($line2, 25,1) eq "N") {

        my($tmp) = unpack("x28 A70", $line2);

        $notes .= $tmp;
        $save = tell FH;
        $line2 = <FH>;

    }

    #
    # have possibly read the first line of a new record, so rewind
    #

    seek FH,$save, 0;

    %airspace = ();

    $airspace{_class} = $class;
    $airspace{_name} = $name;
    $airspace{_notes} = $notes;
    $airspace{_lower} = $low;
    $airspace{_upper} = $high;

    #
    # initial record is held in $line
    #
    
    @bounds=();

    # I've left in the old printf statements for the prior format, just for
    # reference
    
    sub mkLine {
        my ($lat, $lon) = @_;
        #printf AIRSPACE "L%.5f %.5f\n", $lat, $lon;
        my (%b, %coord);
        
        $b{_type} = "line";
        $coord{_lat} = $lat;
        $coord{_lon} = $lon;
        $b{coord}=\%coord;
        push @bounds, \%b;
    }

    sub mkArc {
        #
        # 'draw' line to the start of the arc, then the arc itself
        #

        my ($dir, $lat1, $lon1, $lat2, $lon2, $latc, $lonc) = @_;
        #printf AIRSPACE "L%.5f %.5f\n", $lat1, $lon1 if ($linenum>1);
        #printf AIRSPACE "A$rectype%.5f %.5f %.5f %.5f %.5f %.5f\n",$lat1,$lon1,$lat2,$lon2,$latc, $lonc;
        my (%l, %b, %start, %end, %centre);
        #$l{_type} = "line";
        #$l{_lat} = $lat1;
        #$l{_lon} = $lon1;
        #push @bounds, \%l;
        mkLine($lat1, $lon1);

        $b{_type} = "arc";
        $b{_dir} = $dir;
        
        $start{_lat} = $lat1;
        $start{_lon} = $lon1;
        $end{_lat} = $lat2;
        $end{_lon} = $lon2;
        $centre{_lat} = $latc;
        $centre{_lon} = $lonc;
        
        $b{start}=\%start;
        $b{end}=\%end;
        $b{centre}=\%centre;
        push @bounds, \%b;
    }

    $linenum=1;
    while (1) {

        my %b = ();

        #
        # $rectype will be one of G/H, R,L or C
        #

        $rectype = substr($line, 30,1);
        $lastrecord = (substr($line, 31,1) eq "E") ? 1 : 0;

        #
        # store the first lat/lon reference, which allows us to close
        # up the airspace at the end of the process
        #

        if ($linenum == 1 && $rectype ne "C") {

            ($firstlat, $firstlon) = unpack("x32 A9 A10", $line);
            ($firstlat, $firstlon) = LatLonMV($firstlat,$firstlon);

        }

        if ( $rectype eq "G" || $rectype eq "H") {

            my ($lat, $lon) = unpack("x32 A9 A10", $line);
            ($lat, $lon) = LatLonMV($lat,$lon);

            mkLine($lat, $lon);

            if ($lastrecord) {

                mkLine($firstlat, $firstlon);

            }

        } elsif ($rectype eq "C") {

            my ($lat, $lon,$radius) = unpack("x51 A9 A10 A5", $line);

            ($lat, $lon) = LatLonMV($lat, $lon);
            $radius /= 10;

            #
            # C-record must be the only one
            #

            die if (!$lastrecord);

            #printf AIRSPACE "C%.5f %.5f %.1f\n", $lat, $lon, $radius;
            $b{_type} = "circle";
            $coord{_lat} = $lat;
            $coord{_lon} = $lon;
            $b{centre} = \%coord;
            $b{radius} = $radius;
            push @bounds, \%b;

        } elsif ($rectype eq "R" || $rectype eq "L") {

            my ($lat1, $lon1,$latc, $lonc) = unpack("x32 A9 A10 A9 A10", $line);

            ($lat1, $lon1) = LatLonMV($lat1, $lon1);
            ($latc, $lonc) = LatLonMV($latc, $lonc);

            my ($lat2,$lon2) = (0,0);

            #
            # end of the arc is specified in the next line, unless this is the
            # last record. In that case, the end of the arc is in the first line,
            # which we stored earlier...
            #

            if ($lastrecord) {

                ($lat2, $lon2) = ($firstlat, $firstlon);

                #
                # 'draw' line to the start of the arc, then the arc itself
                #

                mkArc($rectype, $lat1, $lon1, $lat2, $lon2, $latc, $lonc);

            } else { 

                #
                # read the next line
                # 

                $linenum++;
                $line = <FH>;
                $nextrectype = substr($line, 30,1);
                $lastrecord = (substr($line, 31,1) eq "E") ? 1 : 0;

                ($lat2, $lon2) = unpack("x32 A9 A10", $line);
                ($lat2, $lon2) = LatLonMV($lat2,$lon2);

                mkArc($rectype, $lat1, $lon1, $lat2, $lon2, $latc, $lonc);

                #
                # if the record we just read is a G/H record, then we can consume
                # it without further processing - it's not needed anymore. But
                # if the current record is L/R then we need to process it
                #

                if ($nextrectype =~ /[LR]/) {

                    next;

                }

                if ($lastrecord) {

                    mkLine($firstlat, $firstlon);

                }

            } 

        } else {

            die "Unrecognised segment type ($rectype)";

        }

        last if ($lastrecord);

        $linenum++;
        $line = <FH>;

    }

    $airspace{bounds} = \@bounds;
    print AIRSPACE mkJSON(%airspace) . "\n";

}

###############################################################################
            
sub Airway {

    my ($line1) = @_;

    #
    # if this changes during reading the file without an end-of-airway
    # marker (no XX in the line) then the airway record is incomplete
    #

    $airwayIdent = substr($line1, 14,5);

    $segs = 0;

    WHILE: while (substr($line1, 47, 1) eq "X") {

        ($ident, $waypoint1, $id2, $low, $high) = unpack("x13 A5 x11 A5 A2 x47 A5 x5 A5", $line1);
        $type = substr($line1, 45,1);

        # $uid = "$ident-$id2".substr($line,4,2);
        $waypoint1 = "$waypoint1-$id2".substr($line1,36,2);

        #
        # check altitude of this segment, if it's changed since last
        # segment then a new airway record needs to be created
        #

        $low = $lastLow if (!$low);
        $high = $lastHigh if (!$high);

        if ($segs) {

            if ( $low ne $lastLow || $high ne $lastHigh) {

                #print "Airway alt changed at segment $segs\n";
                seek FH,$save,0;
                last WHILE;

            }

        } else {

            $lastLow = $low;
            $lastHigh = $high;

        }

        $save = tell FH;
        $line2 = <FH>;

        if (substr($line2,14,5) ne $airwayIdent) {

            #print "Airway $ident not terminated properly ($segs segments)\n";
            seek FH, $save, 0;
            last WHILE;

        }

        ($waypoint2,$id2) = unpack("x29 A5 A2",$line2);
        $waypoint2 = "$waypoint2-$id2".substr($line2,36,2);

        $line1 = $line2;

        #print "Airway $ident segment $segs from $waypoint1 ($identifier{$waypoint1}) to $waypoint2 ($identifier{$waypoint2}), limits $low $high\n";

        last WHILE if (!$lat{$waypoint1});
        last WHILE if (!$lat{$waypoint2});
        #die "$waypoint1 not defined" if (!$lat{$waypoint1});
        #die "$waypoint2 not defined" if (!$lat{$waypoint2});

        $segs ++;

        #
        # output header
        #

        if ($segs eq 1) {

            $low = AirspaceAltitude($low);
            $high= AirspaceAltitude($high);

            #print AIRSPACE "W$type\n$ident\n-\n50\n$low\n$high\n";
            #printf AIRSPACE "L%.5f %.5f\n", $lat{$waypoint1}, $lon{$waypoint1};

        }

        printf AIRSPACE "L%.5f %.5f\n", $lat{$waypoint2}, $lon{$waypoint2};

    }

    if ($segs) {

            #print AIRSPACE "X\n";

    } 

}

###############################################################################

print "ARINC424 Processor (v2.0)\n(c) 2012 Kevin Haines\n\n";

$file=$ARGV[0];
$outputfile=$ARGV[1];
$units=$ARGV[2];

$waypointFile="$outputfile.way";
$airspaceFile="$outputfile.air";

$runwayFactor = $units eq "metres" ? 3.2808399 : 1.0;

if ( ! -f $file) {

    print "Error: $file not found\n";
    die;

}

open(FH, $file);

#
# Two passes over the file are needed, this is because the ARINC file can use
# waypoint idents in airways before they've been defined as waypoints
#

#
# get the AIRAC cycle number
#

$line=<FH>;
$line=<FH>;
$cycle=substr($line,35,4);
print "ARINC Cycle: $cycle\n";
print "Processing data for regions: @ARGV[4..@ARGV-1]\n";

$coverage=$ARGV[3];

$"="|"; 
$region="@ARGV[4..@ARGV-1]";
$"=" ";

$disclaimer = "NavData supplied by Jeppesen.\n\nCoverage: $coverage\nAIRAC Cycle: $cycle\n\nJeppesen and Blackhawk Systems are not responsible for errors in this database.";

open(AIRSPACE,">$airspaceFile");

#
# first pass, waypoints and airspace
#

print "First pass: processing waypoints & airspace\n";

while (<FH>) {

    ($area, $type) = unpack("x A3 A1",$_);

    #
    # filter by area
    # 

    SWITCH: {

    /^....D..............(..)1/ && do { next if ("$area$1" !~ /($region)/); Navaid($_); last SWITCH; };
    /^....P.....(..)A........1/ && do { next if ("$area$1" !~ /($region)/); Airfield($_); last SWITCH; };
    /^....P.....(..)GRW......1/ && do { next if ("$area$1" !~ /($region)/); Runway($_); last SWITCH; };
    /^....P.....(..)I........0/ && do { next if ("$area$1" !~ /($region)/); ILS($_); last SWITCH; };
    /^....P.....(..)V...........V/ && do { next if ("$area$1" !~ /($region)/); CommFrequency($_); last SWITCH; };
    /^....EA.............(..)1/ && do { next if ("$area$1" !~ /($region)/); Waypoint($_); last SWITCH; };
    /^....PN.............(..)1/ && do { next if ("$area$1" !~ /($region)/); Navaid($_); last SWITCH; };
    /^....U[RC](..)...........[^Z].....[LHB]/ && do { next if ("$area$1" !~ /($region)/); Airspace($_); last SWITCH; };

    }

}

close(FH);

#
# second pass, airways
#

#open(FH, $file);

#print "Second pass: processing airways.\n";
#while (<FH>) {

#($area, $type) = unpack("x A3 A1",$_);
#
#SWITCH: {
#
#/^....ER............................(..)/ && do { next if ("$area$1" !~ /($region)/); Airway($_); last SWITCH; };
#
#}
#
#}

#close(FH);

close(AIRSPACE);

$disclaimer.="\n\nRunway dimensions: $units";

open(WAYPOINT,">$waypointFile");
#open(WAYPOINT,">tmp.way");



print "\nCreating waypoint database with ".scalar(keys(%identifier))." waypoints.\n";
#foreach $k (sort {$waypoint{$a} <=> $waypoint{$b} } keys %waypoint) {
foreach $k (sort keys %identifier) {

    # {"mv":-197,"coord":{"lon":"-002030500","lat":"+051400000"},"ident":"EGBP","alt":0,"info":{"NAME":"KEMBLE"}}
    $coord{_lat} = $lat{$k};
    $coord{_lon} = $lon{$k};
    $info{_name} = $name{$k};
    $info{_type} = $type{$k};

    $json{_ident} = $identifier{$k};
    $json{magVar} = $magv{$k};
    $json{coord} = \%coord;
    $json{info} = \%info;
    $json{altitude} = 0;
    
    print WAYPOINT mkJSON(%json)."\n";

    # Old format left here for reference
    #print WAYPOINT "$identifier{$k} $lat{$k} $lon{$k} $magv{$k} $alt{$k}\n";
    #print WAYPOINT "$name{$k}\n";

    #print WAYPOINT "$notes{$k}\n";

    #print WAYPOINT "$runways{$k}\n" if ($runways{$k});
    #print WAYPOINT "\nRight-hand: $trafficPatterns{$k}\n\n" if ($trafficPatterns{$k});
    #print WAYPOINT "$ils{$k}\n" if ($ils{$k});    

    #print WAYPOINT "XXX\n";

}

close(WAYPOINT);


#print "Creating zip file\n";
#system("zip $outputfile$cycle.zip $waypointFile $airspaceFile");
