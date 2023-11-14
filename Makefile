all: set clean compile

set:
	
	# Setting...
	@mkdir -p lib/
	@mkdir -p classes/

clean:
	
	# Cleaning...
	@rm -f -r lib/*
	@rm -f -r classes/*

compile:
	
	# Compiling the classes...
	@javac -Xlint -d classes/ src/crowdchat/*.java
	# Creating client exec jar...
	@cd classes/ \
		&& jar cvfe ../lib/Application.jar crowdchat.Application \
		crowdchat/Application* crowdchat/Linker* crowdchat/Client* \
		../assets
	# Creating server exec jar...
	@cd classes/ \
		&& jar cvfe ../lib/Server.jar crowdchat.Server \
		crowdchat/Server* crowdchat/Linker* crowdchat/Client*
