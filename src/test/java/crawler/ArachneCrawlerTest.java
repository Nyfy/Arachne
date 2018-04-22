package crawler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import crawler.ArachneController;
import crawler.ArachneCrawler;
import seeds.Seed;

class ArachneCrawlerTest {
    
    @Mock
    private static ArachneController mockController;
    @Mock
    private static WebDriver mockWebDriver;
    @Mock
    private static WebDriverWait mockWaitShort;
    @Mock
    private static WebDriverWait mockWaitLong;
    @Mock
    private static Seed mockSeed;
    
    private static ArachneCrawler crawler;
    
    private static List<String> testPagesToVisit;
    private static List<String> testPagesToProcess;
    private static List<String> testPagesProcessed;
    
    @BeforeAll
    public static void setupAll() {
        testPagesToVisit = new ArrayList<String>();
        testPagesToVisit.add("visitPage1");
        testPagesToVisit.add("visitPage2");
        testPagesToVisit.add("visitPage3");
        
        testPagesToProcess = new ArrayList<String>();
        testPagesToProcess.add("processPage1");
        testPagesToProcess.add("processPage2");
        testPagesToProcess.add("processPage3");
        
        testPagesProcessed = new ArrayList<String>();
        testPagesProcessed.add("processPage3");
    }
    
    @BeforeEach
    public void setupEach() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        crawler = new ArachneCrawler(mockController, false);
        crawler.setupTest(mockWebDriver, mockWaitShort, mockWaitLong,
                testPagesToVisit, testPagesToProcess, testPagesProcessed);
    }
    
    @Test
    public void testRun() {
        List<String> seedUrls = new ArrayList<String>();
        seedUrls.add("testUrl");
        
        Mockito.when(mockSeed.getSeedUrls()).thenReturn(seedUrls);
        
        crawler.addSeed(mockSeed);
        crawler.run();
        
        Mockito.verify(mockSeed, Mockito.times(1)).getSeedUrls();
        Mockito.verify(mockWebDriver, Mockito.times(1)).get("testUrl");
        Mockito.verify(mockWebDriver, Mockito.times(1)).get(Mockito.anyString());
    }
    
    @Test
    public void testProcessResults() throws Exception {
        String testTopic = "testTopic";
        String resultA = "resultA";
        String resultB = "resultB";
        String resultC = "resultC";
        
        Mockito.when(mockSeed.getTopic()).thenReturn(testTopic);
        Mockito.when(mockSeed.processResult(Mockito.any(WebDriver.class), Mockito.any(WebDriverWait.class), Mockito.any(WebDriverWait.class)))
            .thenReturn(resultA).thenReturn(resultB).thenReturn(resultC);
        
        crawler.processResults(mockSeed);
        
        for (String pageToProcess : testPagesToProcess) {
            Mockito.verify(mockWebDriver, Mockito.times(1)).get(pageToProcess);
        }
        
        Mockito.verify(mockController, Mockito.times(1)).sendResult(resultA, testTopic);
        Mockito.verify(mockController, Mockito.times(1)).sendResult(resultB, testTopic);
        Mockito.verify(mockController, Mockito.times(1)).sendResult(resultC, testTopic);
    }
    
    @Test
    public void testShouldVisit() {
        Mockito.when(mockSeed.getShouldVisitRegex()).thenReturn("^visitPage\\d$");
        
        boolean shouldVisitTrue = crawler.shouldVisit(mockSeed, testPagesToVisit.get(1));
        boolean shouldVisitFalse = crawler.shouldVisit(mockSeed, "nonmatching");
        
        assertTrue(shouldVisitTrue);
        assertFalse(shouldVisitFalse);
    }
    
    @Test
    public void testShouldProcess() {
        Mockito.when(mockSeed.getShouldProcessRegex()).thenReturn("^process(Page\\d)$");
        
        String shouldProcessTrue = crawler.shouldProcess(mockSeed, "processPage5");
        String shouldProcessDuplicate = crawler.shouldProcess(mockSeed, "processPage3");
        String shouldProcessFalse = crawler.shouldProcess(mockSeed, "nonmatching");
        
        assertTrue(StringUtils.equalsIgnoreCase(shouldProcessTrue, "page5"));
        assertEquals(shouldProcessDuplicate, null);
        assertEquals(shouldProcessFalse, null);
    }

}
