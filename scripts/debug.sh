java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar rt-front-web-0.1.war > rt.log 2>rt.out &
