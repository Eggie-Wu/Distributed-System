echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - name of the middleware'
echo '  $2 - hostname of Flights'
echo '  $3 - hostname of Cars'
echo '  $4 - hostname of Rooms'

java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.TCP.TCPMiddleware $1 $2 $3 $4