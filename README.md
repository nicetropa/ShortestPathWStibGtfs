## Place GTFS Data
    Copy the GTFS folder to data/.

## Build Project
```bash
    make
```

## Launch program 
```bash
    ./run.sh DepartureStop ArrivalStop hh:mm:ss
```

If your stop has spaces place quotes arround it
```bash
    ./run.sh "Departue Stop" ArrivalStop hh:mm:ss
```