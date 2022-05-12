FROM openjdk:8-jdk-alpine
##EXPOSE 8001
ADD target/*.jar api-bankaccount.jar
ENTRYPOINT ["java","-jar","api-bankaccount.jar"]