# we will use openjdk 8 with alpine as it is a very small linux distro
FROM adoptopenjdk/openjdk8:ubi

ADD . . 

RUN javac -cp "./jgroups-3.6.14.Final.jar" ./src/com/nathanial/auction/*.java -d out

WORKDIR ./out
CMD ["java", "com.nathanial.auction.ClientSeller"]

EXPOSE 1099