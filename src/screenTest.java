/**
 * Created by wolfman on 4/9/16.
 */
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.screentaker.ViewportPastingStrategy;

import javax.imageio.ImageIO;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by chrx on 11/20/15.
 */
public class screenTest{

    static WebDriver driver;
    static String URL ;
    static Properties prop;
    static StringBuilder result;




    public static void setUp() throws IOException, InterruptedException {

        prop = loadProp();
        URL = prop.getProperty("URL");
        String BROWSER = prop.getProperty("browser");

        if (new File(prop.getProperty("results")).exists()){
            new File(prop.getProperty("results")).delete();
        }


        if(BROWSER.equals("phantom")){
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    prop.getProperty("phantomDriverLocation"));
            driver = new PhantomJSDriver(caps);
            driver.manage().window().setSize(new Dimension(1600, 1200));
        }

        else if (BROWSER.equals("chrome")) {

            System.setProperty("webdriver.chrome.driver", prop.getProperty("chromeDriverLocation"));

            ChromeOptions options;

            options = new ChromeOptions();
            options.addArguments("disable-plugins");
            options.addArguments("disable-extensions");
            options.addArguments("--disable-internal-flash");
            options.addArguments("--disable-bundled-ppapi-flash");
            options.addArguments("--disable-plugins-discovery");

            options.addArguments("--mute-audio");
            driver = new ChromeDriver(options);

        } else if (BROWSER.equals("IE")) {

            System.setProperty("webdriver.ie.driver", "IEDriverServer.exe");
            DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
            caps.setCapability("ignoreZoomSetting", true);
            driver = new InternetExplorerDriver(caps);

        } else {

            FirefoxProfile firefoxProfile = new FirefoxProfile();
            firefoxProfile.setPreference("media.volume_scale", "0.0");
            driver = new FirefoxDriver(firefoxProfile);
        }
        driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
        //driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

        if((!BROWSER.equals("phantom"))){

            driver.manage().window().maximize();

        }

        if (prop.getProperty("device").equals("mobile")) {
            driver.manage().window().setSize(new Dimension(400, 3000));
        }

        Thread.sleep(2000);
//        driver.get(URL);
        //    URL = prop.getProperty("URL");
        //    driver.navigate().to(URL);
    }

    public static void main(String[]args) throws IOException, InterruptedException {

        setUp();

        // This part is to read data from file
        String ID;
        System.out.println("0");

        File testDataSrc = new File(prop.getProperty("testDataLocation"));
        FileInputStream testData = new FileInputStream(testDataSrc);
        XSSFWorkbook wb = new XSSFWorkbook(testData);
        XSSFSheet sheet1 = wb.getSheetAt(0);


//        File testDataSrcResults = new File(prop.getProperty("testDataLocationResults"));
        //FileInputStream testDataResults = new FileInputStream(testDataSrcResults);
        // FileOutputStream testDataResults = new FileOutputStream(testDataSrcResults);
        // XSSFWorkbook wbResults = null;

        /*try {
            wbResults = new XSSFWorkbook(testDataSrcResults);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        XSSFSheet sheet1Results = wbResults.getSheetAt(0);
*/
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy - hh:mm");
        String time = dateFormat.format(now);

        // location = prop.getProperty("ScreenshotLocation");

        File newPath = new File(prop.getProperty("ScreenshotLocation") +" - " + time);

        if (!newPath.exists()) {
            if (newPath.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }


        String templateName;


        //  FileOutputStream fileOut = new FileOutputStream(prop.getProperty("testDataLocationResults"));


        System.out.println("Physical Number of Rows:" + (sheet1.getPhysicalNumberOfRows()-1));

        int limit = 0;

        try{

            limit = Integer.parseInt(prop.getProperty("limit"));

        }catch (Exception e){

            System.out.println("No limit provided using default limit (Size of excel)");
            limit = (sheet1.getPhysicalNumberOfRows()-1);
        }

        for (int i = 1; i <=  /*(sheet1.getPhysicalNumberOfRows()-1)*/ limit ; i++) {

            ID = sheet1.getRow(i).getCell(1).getStringCellValue();
            long startTime = System.currentTimeMillis();

            if (prop.getProperty("device").equals("desktop")) {
                driver.get(URL + "/id/" + ID);

                if (prop.getProperty("X").equals("xfinity")) {
                    if (i < 2) {
                        Cookie newCookie = new Cookie("active_partner_exp", "xfinity", "/");
                        driver.manage().addCookie(newCookie);
                        driver.navigate().refresh();
                        Thread.sleep(3000);
                    }

                }
            }

            else if (prop.getProperty("device").equals("mobile")) {
                driver.get(URL + "/id/" + ID + "?$DEVICE$=mobile-touch");
            }

            else if (prop.getProperty("device").equals("tablet")) {
                driver.get(URL + "/id/" + ID + "?$DEVICE$=mobile-tablet");
            }

            else if (prop.getProperty("device").equals("android")) {
                driver.get(URL + "/id/" + ID + "?$DEVICE$=mobile-tablet");
            }


            long endTime = System.currentTimeMillis();

            Thread.sleep(5000);

            JavascriptExecutor je = (JavascriptExecutor) driver;


            //ASnap Scrolls on whole page

         /*   je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("window.scrollBy(0,400)", "");
            je.executeScript("arguments[0].scrollIntoView(true);",Ad1.getFooterLogo());*/

           // Thread.sleep(2000);

            // Screenshot of File

            //File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            //  File scrFile1 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            //  File scrFile2 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Copy folder to new Path

            Screenshot screenshot = new AShot().shootingStrategy(new ViewportPastingStrategy(1000)).takeScreenshot(driver);


            templateName = sheet1.getRow(i).getCell(0).getStringCellValue();
            templateName = templateName.replace(' ', '-');

            if (prop.getProperty("device").equals("desktop")) {

                File directory = new File(newPath + "/" + i +" - "+ templateName);
                directory.mkdir();

                ImageIO.write(screenshot.getImage(), "PNG", new File(directory +"/" +templateName + ".png"));
                // FileUtils.copyFile(scrFile, new File(newPath + "/" + templateName + "/" + templateName + ".png"));
            } else {

                ImageIO.write(screenshot.getImage(), "PNG", new File(newPath + "/" + i +" - MOBILE- "+ templateName + "/" + templateName +  "- MOBILE.png"));
                // FileUtils.copyFile(scrFile, new File(newPath + "/" + templateName + "- MOBILE.png"));
            }
            System.out.println(newPath);
            System.out.println("Screenshot of :" + "/" + templateName + " Done.");


            long totalTime = (endTime - startTime) / 1000;

            System.out.println("Total Page Load Time: " + totalTime + "milliseconds");

        }


        //fileOut.close();
        // Open the folder of screenshots

        tearDown();

    }


    public static Properties loadProp() throws IOException {

        File file = new File("resources/screenConfig.prop");
        FileInputStream fileInput = new FileInputStream(file);
        Properties prop = new Properties();
        prop.load(fileInput);
        fileInput.close();

        return prop;
    }




    public static void tearDown() throws IOException {

        driver.quit();


    }

}
