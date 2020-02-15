docker build -t uts/database .
docker run -p 5432:5432 -d --name uts-database uts/database
cd ..
gradlew.bat assemble
powershell Expand-Archive data-load/build/distributions/data-load-1.0-SNAPSHOT.zip .
cd data-load-1.0-SNAPSHOT/bin
data-load.bat -ie -u postgres -p postgres
data-load.bat -cd
data-load.bat -lt -c 10 -bd %full path of tennis_atp data directory%
docker stop uts-database
docker commit -m "UTS data load" uts-database mcekovic/uts-database
docker tag mcekovic/uts-database mcekovic/uts-database:asof2019
docker push mcekovic/uts-database
docker push mcekovic/uts-database:asof2019