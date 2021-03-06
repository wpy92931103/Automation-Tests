# Automation-Tests

Getting started:

Create a workspace dir and cd into it.

To clone the repo from the cmd line: 

	git clone https://github.com/Mercatus/Automation-Tests
	
Recommend installing SourceTree, a GUI for Git repos.
	
	https://www.sourcetreeapp.com/	
	
In Eclipse, File > New > Java Project. Enter the name of the repo for the Project name so that it's in synch with Github.

![Alt text](readmeScreenshots/create_java_project.png?raw=true)

Copy dependencies from pom.xml to your local box. In the pom.xml, temporarily comment out the suiteXmlFile. 

	cd Automation-Tests
	mvn dependency:copy-dependencies -Dclassifer=sources
	cp target/dependencies Automation-Tests 

In Eclipse, Help > Install New Software for TestNG:

![Alt text](readmeScreenshots/install_testng.png?raw=true)

In Eclipse, configure Java Build Path:

![Alt text](readmeScreenshots/java_build_path_source.png?raw=true)

![Alt text](readmeScreenshots/libraries.png?raw=true)

Convert TestRail test cases to TestNG methods. Args: projectId suiteId startTCID endTCID

![Alt text](readmeScreenshots/get_test_cases.png?raw=true)

As a guideline, see examples of:

	Automation-Tests/src/test/java/com/automation/selenium/SeleniumUtils.java
	Automation-Tests/src/test/java/com/automation/pageObjs
	Automation-Tests/src/test/java/com/automation/tests
	Automation-Tests/src/test/java/com/automation/workflows

Configure testng_mercatus.xml

![Alt text](readmeScreenshots/config_testng.png?raw=true)

From the cmd line, launch the Selenium Grid from the Automation-Tests dir:

	bash shell\ scripts/startSeleniumGrid.sh

Run testng_mercatus.xml

![Alt text](readmeScreenshots/run_testng.png?raw=true)

After the test run, refresh Automation-Tests dir to see the results under Automation-Tests/test-output

To run API tests:

Install JMeter:

	http://jmeter.apache.org/download_jmeter.cgi
	Binaries > apache-jmeter-3.0.zip
	unzip the file
	mv ~/Downloads/apache-jmeter-3.0 /Applications
	
Launch JMeter from the cmd line:

	cd to Automation-Tests dir
	check the JMeter version in shell scripts/jmeter.sh
	bash shell\ scripts/jmeter.sh	
		 
The 2 main files in Automation-Tests/src/test/jmeter are:		 

	api.tests.csv
	API_test.jmx
	
api.tests.csv are the test cases that API_test.jmx will read and run:
	
![Alt text](readmeScreenshots/jmeter_csv.png?raw=true)
	
In API_test.jmx, the Test Plan node contains the User Defined Variables:
	
![Alt text](readmeScreenshots/jmeter_test_plan.png?raw=true)

To run the test from the GUI:
	
![Alt text](readmeScreenshots/jmeter_run.png?raw=true)		

View Results Tree
	
![Alt text](readmeScreenshots/view_results_tree.png?raw=true)		

Jenkins Build Parameters
	
![Alt text](readmeScreenshots/jenkins.png?raw=true)		

