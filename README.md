# meeboo

mvn clean
mvn install

docker build -f Dockerfile -t usersystem .
docker tag usersystem seankelly1982b/usersystem
docker push seankelly1982b/usersystem
ssh root@
docker ps
docker system prune
docker pull seankelly1982b/usersystem
docker run -d -p 8082:8082 --restart always seankelly1982b/usersystem