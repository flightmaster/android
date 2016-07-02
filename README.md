# FlightMaster

## Setup

You will need a Google Maps API key to build and run FlightMaster.

https://developers.google.com/maps/documentation/android-api/signup


Define the following properties in your gradle.properties file:

- debugMapApiKey=*key*
- releaseMapApiKey=*key*
- debugKeyStore=/home/myname/debug.keystore
- debugKeyAlias=androiddebugkey
- debugKeyStorePassword=""
- debugKeyPassword=""
- releaseKeyStore=/home/kevin/debug.keystore
- releaseKeyAlias=androidreleasekey
- releaseKeyStorePassword=""
- releaseKeyPassword=""

Keystore settings can be commented out of the gradle.build file as necessary.

## Building

    gradle assembleDebug

This will build and sign a debug version of FlightMaster, output is in:

    build/outputs/apk

## Developing

The code is a year old as of June 2016, and has not been developed since June
2015.  It is not in very good shape, being an experimental prototype that I
used to learn Java and Android development at the same time.

Problems:

- The architecture was an attempt to keep the code portable, a decision I now
  regret.

- The serialisation of databases has too much boilerplate code. I tried default
  serialisation but it was very slow. There must be a better way, eliminating
  the code overhead.

- Testing (unit and UI) needs coverage, that said it is a prototype.

- Dependency Injection hasn't been used anywhere; Dagger is probably a good candidate

- Lombok has been used, I'd prefer something like Immutables to get the effect

- Tried to keep LatLng (a maps-specific primitive class) out of the core classes
  has resulted in a more complex code base than needs be; goes back to the 
  attempt to keep the architecture platform independent which it needn't be.

- Many more, I'm sure

