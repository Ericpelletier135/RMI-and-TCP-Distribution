all: java.policy compile-server-TCP

java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

compile-server-TCP: Server/TCP/*.java Server/Common/*.java Server/TCP/*.java
	javac Server/TCP/*.java Server/Common/*.java Server/TCP/*.java

clean:
	rm -f Server/Common/*.class Server/TCP/*.class
	rm -f java.policy 
