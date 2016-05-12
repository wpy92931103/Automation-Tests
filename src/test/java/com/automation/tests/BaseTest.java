package com.automation.tests;

import com.automation.config.TestConfiguration;
import com.automation.pageObjs.LoginPage;
import com.automation.selenium.Driver;
import com.automation.selenium.SauceLabs;
import com.automation.selenium.ScreenshotOnFailure;
import com.automation.selenium.SeleniumUtils;
import com.automation.testng.Retry;
import com.automation.utils.ExecuteShellCommand;
import com.automation.utils.FileUtilities;
import com.automation.utils.Log4J;
import com.automation.utils.SlackNotifications;
import com.automation.utils.SparkNotifications;
import com.gurock.testrail.GetTestCases;
import com.gurock.testrail.TestRailUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.UUID;

public class BaseTest extends TestRailUtilities {
	
	protected final static Logger logger = Logger.getLogger(Log4J.class);
	private static ThreadLocal<String> methodName = new ThreadLocal<String>();
	private static ThreadLocal<String> testCaseId = new ThreadLocal<String>();
	private static ThreadLocal<String> testDataName = new ThreadLocal<String>();
	private static ThreadLocal<String> maxTestDataName = new ThreadLocal<String>();
	private static ThreadLocal<String> caseResults = new ThreadLocal<String>();
	private static ThreadLocal<Boolean> mxTitles = new ThreadLocal<Boolean>();
	boolean methodNameCorrect = false;	
	Hashtable<String, ITestResult> testResults = new Hashtable<String, ITestResult>();
	StringWriter errors = new StringWriter();
	StringBuilder sb = new StringBuilder();
	public static boolean updTestRail = false;
	public static String suiteName = null;
	public static String project = null;
	public static String projectId = null;
	public static String suiteId = null;
	public static String env = null;
	public static String runId = null;
	public static String database = null;
	public static boolean sauceLabs = false;
	public static boolean addRun = false;
	
	public String getTestEnv(String testEnv, boolean tag) {
		
		String env = System.getenv("ENV");
		if (env != null) {
			testEnv = env;
		}
		if (tag) {
			return testEnv;
		} else {
			return testEnv + " - ";
		}
	}
	
	public static String getMethodName() {
		String[] s = methodName.get().split("_");
		testCaseId.set(s[0]);
        return methodName.get();
    }
	
	public static String getTestCaseId() {
		return testCaseId.get();
    }
	
	public static String getTestDataName() {
		return testDataName.get();
    }
	
	public static String getMaxTestDataName() {		
		return maxTestDataName.get();
    }
	
	public static boolean getMaxTitles() {
		try {
			return mxTitles.get();	
		} catch (Exception e) {
			return false;	
		}		
    }

	public String getRandomUUIDString() {
		String randomUUIDString = UUID.randomUUID().toString();
		return randomUUIDString;
	}
	
	public String getTestCaseName() {		
		String testCaseName = BaseTest.getMethodName(); 		
		if (BaseTest.getMethodName().length() > 100) {
			testCaseName = testCaseName.substring(0,100);
		}		
		return testCaseName;
	}
	
	public static String getBuildUrl() {
		String buildUrl = System.getenv("BUILD_URL");
		if (buildUrl == null) {
			buildUrl = "";
		}
		return buildUrl;
	}
	
	public static String getCaseResults() {		
		String cr = caseResults.get();
		if (cr != null) {
			return cr;
		} else {
			return "";
		}
    }
		
	public void setActors() {
	}
	
	@BeforeSuite(alwaysRun = true)
	@Parameters({ "projectId", "suiteId", "env", "updateTestRail", "addRun", "runId", "sauceLabs", "browser", "database" }) 
	public void beforeSuite(String projectId, String suiteId, String env, boolean updateTestRail, boolean addRun, String runId, boolean sauceLabs, String browser, @Optional String database) {		
		String[] suite = this.getClass().getName().split("\\.");
		BaseTest.suiteName = suite[suite.length - 1];
		BaseTest.project = suite[suite.length - 2];
		BaseTest.projectId = projectId; 
		BaseTest.suiteId = suiteId;
		BaseTest.env = env.replace(".conf", "");
		BaseTest.sauceLabs = sauceLabs;
		BaseTest.updTestRail = updateTestRail;	
		BaseTest.addRun = addRun;
		BaseTest.runId = runId;
		BaseTest.database = database;
		
		FileUtilities fu = new FileUtilities();		
		File dir = new File("test-output");
		try {
			FileUtils.deleteDirectory(dir);
			logger.info("deleted the dir " + dir);
		} catch (IOException e) {
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}
		new File("test-output").mkdir();
		fu.createFile("test-output/log4j-application.log");
		org.apache.log4j.PropertyConfigurator.configure("src/test/resources/log4j.properties");
		
		if (updTestRail && addRun) {
			BaseTest.runId = addRun(getTestEnv(env, false), browser);
			String[] cmd = new String[] {"sed", "-i.tmp", "s/RUNID/"+BaseTest.runId+"/g", "src/test/resources/testng/testng.xml"};
			ExecuteShellCommand es = new ExecuteShellCommand();
			es.executeArrayCommand(cmd);
		}			
		if(!env.equals("default"))
			TestConfiguration.setConfig(env);		
		
		createTestNGfailed(fu);				
	}

	@BeforeMethod(alwaysRun = true)
	@Parameters({ "name", "platform", "browser", "version", "deviceName", "deviceOrientation", "who" })			
	public void setup(@Optional String name, @Optional Platform platform,
			@Optional String browser, @Optional String version,
			@Optional String deviceName, @Optional String deviceOrientation, @Optional String who, Method method)			
			throws MalformedURLException {
		
		setActors();
		methodName.set(method.getName());
		
		if (updTestRail) {
			if (getMethodName().equals(getCaseId(method))) {
				Assert.assertEquals(getMethodName(), getMethodName() + " does not conform to the method naming convention. expected c${test rails test case id}_${desc}");
			} else {
				methodNameCorrect = true;
			}
		}
		
		String tc = "";
		if (!this.getTestCaseName().contains("verify_")) {
			tc = "verify_" + this.getTestCaseName();	
		} else {
			tc = this.getTestCaseName();	
		}	
		testDataName.set(tc + "-" + this.getRandomUUIDString());
		
		SeleniumUtils su = new SeleniumUtils();
		maxTestDataName.set(BaseTest.getTestDataName() + su.getRandomString(255 - BaseTest.getTestDataName().length()));
		
		Driver.createDriver(name, getTestEnv(env, true), platform, browser, version, deviceName,
				deviceOrientation, sauceLabs, method);
		
		Driver.getDriver().manage().window().maximize();		
		try {			
			LoginPage login = new LoginPage();
			who = who != null ? who : "login";
			login.login();	
		} catch (Exception e) {
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}		
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestContext context, ITestResult result, Method method) throws IOException {
		
		String sauceLabsJobIdLink  = "";
		
		//will display testId for faster tracking
		this.getCaseResults(projectId, suiteId, getCaseId(method));
		
		if (sauceLabs) {
			SauceLabs sl = new SauceLabs();
			String jobId = null; 
			try {
				jobId = sl.getJobId(method);
			} catch (Exception e) {
				jobId = null;
			}
			String steps;
			if (updTestRail) {
				steps = GetTestCases.getAutomatedTestCaseSteps(getSuiteName(false, BaseTest.suiteId), method.getName());
			} else {
				steps = GetTestCases.getAutomatedTestCaseSteps(BaseTest.suiteName, method.getName());
			}
			logger.info(steps);
			String dupResults = BaseTest.getCaseResults();
			if (steps.contains("BaseTest.getCaseResults") && dupResults.contains("Passed")) {
				sauceLabsJobIdLink = "The Sauce Labs session is of a similar test case referenced in the steps. " + dupResults;
			} else if (jobId != null) {
				testResults.put(jobId, result);
				sauceLabsJobIdLink = sl.getLinkToSauceLabJobId(jobId);
				logger.info("\n" + method.getName() + " - " + sauceLabsJobIdLink + "\n");
			} else {
				sauceLabsJobIdLink = "";
			}
		} 
		
		if (updTestRail) {
			if (runId == null) {				
				runId = this.getRunId(BaseTest.runId, BaseTest.projectId, BaseTest.suiteId);
			}					
			if (methodNameCorrect) {					
				if (result.getStatus() == ITestResult.SUCCESS) {				
					uploadResults(method, result, "", sauceLabsJobIdLink);
				}
				updateCase(method, "1", result, sauceLabsJobIdLink); // 1 = Automated
			}
		}
		if (Retry.retryCount == Retry.MAXRETRYCOUNT && result.getStatus() == ITestResult.FAILURE) {
			ScreenshotOnFailure ss = new ScreenshotOnFailure();
			try {
				ss.takeScreenShotOnFailure(result, Driver.getDriver(), method, sauceLabsJobIdLink);
			} catch (Exception e) {}
			
			//ChatNotifications cn = new ChatNotifications();
			SlackNotifications cn = new SlackNotifications();
			String msg = "failed - " + getTestEnv(env, false)
					+ BaseTest.suiteName + " - " + BaseTest.getMethodName();

			if (updTestRail) {
				String testId = getTestId(BaseTest.projectId, BaseTest.suiteId,
						getCaseId(method));
				String testResultLink = "http://testrail.automation.com/index.php?/tests/view/"
						+ testId;
				if (testId == null) {
					testResultLink = "";
				}
				cn.postMsg(msg + " - " + testResultLink);
			} else if (sauceLabs) {
				cn.postMsg(msg + " - " + sauceLabsJobIdLink);			
			}
		}
		this.appendSkippedAndFailedTests(result, method);
		this.removePassedTestsFromTestNG(result, method);
		try {
			if(Driver.getDriver() != null) {
				Driver.getDriver().quit();
			}
		} catch (Exception e) {
			/*
			logger.error("EXCEPTION: Driver.getDriver().quit();");
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
			*/
		}			
	}
	
	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		if (sauceLabs) {
			SauceLabs sl = new SauceLabs();
			sl.createShellScriptUpdateResults(testResults);
		} else {
			logger.info("see test-output/screenshots for failed tests\n");
		}
		this.writeSkippedAndFailedTests();
		if (updTestRail) {
			//this.closeRun(runId);
		}
		String[] command = new String[] {"sed", "-i.tmp", "s/\\<include name\\=\\\"\\.\\*\\\" \\/\\>//2g", "testng_retryFailed.xml"};
		ExecuteShellCommand es = new ExecuteShellCommand();
		es.executeArrayCommand(command);
	}	
	
	public void appendSkippedAndFailedTests(ITestResult result, Method method) {
		if ((!sb.toString().contains(method.getName()) && result.getStatus() == ITestResult.FAILURE && Retry.retryCount == Retry.MAXRETRYCOUNT) || result.getStatus() == ITestResult.SKIP) {
			sb.append("<include name=\"" + method.getName() + "\" />\n");
			addFailedTestsToTestNG(method);
		} 
	}	
	
	public void writeSkippedAndFailedTests() {		
        BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter("test-output/failedAndSkippedTests.txt"));
			output.write("<methods>\n" + sb.toString() + "</methods>");
	        output.close();
	        logger.info("list of failed and skipped test cases are saved in failedAndSkippedTests.txt. add to testng.xml to rerun only those test cases for debugging");
		} catch (IOException e) {
			logger.error("writeSkippedAndFailedTests");
			e.printStackTrace(new PrintWriter(errors));
			logger.error(errors);
		}        
	}
	
	public void loginAsAdmin() {
		String url = TestConfiguration.getConfig().getString("login.url");
		Driver.getDriver().get(url + "#/admin");	
	}
	
	public void loginAsUser() {
		String url = TestConfiguration.getConfig().getString("login.url");
		Driver.getDriver().get(url);	
	}	
	
	public void addFailedTestsToTestNG(Method method) {
		String[] command = new String[] {"sed", "-i.tmp", "s/\\<include name\\=\\\"\\.\\*\\\" \\/\\>"
				+ "/\\<include name\\=\\\""+method.getName()+"\\\" \\/\\>"
				+ "\\\n\\<include name\\=\\\"\\.\\*\\\" \\/\\>/2g", "src/test/resources/testng/testng_retryFailed.xml"};
		ExecuteShellCommand es = new ExecuteShellCommand();
		es.executeArrayCommand(command);
	}
	
	public void removePassedTestsFromTestNG(ITestResult result, Method method) {
		if (result.getStatus() == ITestResult.SUCCESS) {
			String[] command = new String[] {"sed", "-i.tmp", "s/\\<include name\\=\\\""+method.getName()+"\\\" \\/\\>//g", "testng_retryFailed.xml"};
			ExecuteShellCommand es = new ExecuteShellCommand();
			es.executeArrayCommand(command);
		}
	}
	
	public void createTestNGfailed(FileUtilities fu) {
		String currentTestNG = null;
		String testngDir = System.getProperty("user.dir") + "/src/test/resources/testng/";
		try {
			currentTestNG = fu.scanFiles(testngDir, "<parameter name=\"suiteId\" value=\""+suiteId+"\"");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		File from = new File(testngDir + currentTestNG);		
		try {
			fu.deleteExistsFile(testngDir + "testng_retryFailed.xml");
			File to = new File(testngDir + "testng_retryFailed.xml");
			FileUtilities.copyFile(from, to);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}