import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.bonigarcia.wdm.WebDriverManager;
import objects.WinnerDetails;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utilities.FileOptions;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Tester {
    private static Properties props = new Properties();

    public static void main(String[] args) throws IOException, GitAPIException, InterruptedException {
        if(args.length > 0 && args[0] != null && !args[0].isEmpty()){
            File propsFile = new File(args[0]);
            if(propsFile.exists()) {
                props = new Properties();
                props.load(new FileInputStream(propsFile));
            }
        }
//        String testClientPath = "file:///Users/jarndt/code_projects/PokemonShowdown/Pokemon-Showdown-Client/testclient.html";
        String testClientPath = null;
        if(props.getProperty("testclient.path") != null)
            testClientPath = props.getProperty("testclient.path");
        else {
            String destDir = System.getProperty("user.dir")+"/Pokemon-Showdown-Client/";
            if(props.getProperty("dest.dir") != null)
                destDir = props.getProperty("dest.dir");
            String url = "https://github.com/mgutin/Pokemon-Showdown-Client.git", branch = "jonathans-branch";
            if(props.getProperty("git.url")!=null)
                url = props.getProperty("git.url");
            if(props.getProperty("branch.name")!=null)
                branch = props.getProperty("branch.name");
            CloneCommand git = Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(new File(destDir));
            if(!branch.isEmpty())
                git.setBranchesToClone(Collections.singleton(branch))
                    .setBranch(branch);
            if(new File(destDir).exists()){
                Repository r = new FileRepositoryBuilder()
                        .addCeilingDirectory(new File(destDir))
                        .findGitDir(new File(destDir))
                        .build();
                if(r.getAllRefs().containsKey("HEAD"))
                   org.eclipse.jgit.api.Git.wrap(r).pull().call();
            }else
                git.call();
            testClientPath = destDir+"testclient.html";

            new ProcessBuilder("node", "build")
                    .inheritIO()
                    .directory(new File(destDir))
                    .start()
                    .waitFor();

//            File file = new File(System.getProperty("user.dir") + "/pokemon-showdown-client.zip");
//            if (!file.exists()) {
//                InputStream link = (Tester.class.getResourceAsStream("pokemon-showdown-client.zip"));
//                Files.copy(link, file.getAbsoluteFile().toPath());
//                unzip(file.getAbsoluteFile().getAbsolutePath(),destDir);
//            }
//            testClientPath = file.getAbsolutePath();
        }


        testClientPath = "file:///"+testClientPath;
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(
                new ChromeOptions()
                        .addArguments("--disable-notifications")
//                        .addArguments("--headless")
//                        .addArguments("window-size=1920,1080")
        );
        driver.get(testClientPath);
        if(driver instanceof PhantomJSDriver) {
            ExecutorService e = Executors.newSingleThreadExecutor();
            e.submit(() -> {
                while (true)
                    try {
                        Document doc = Jsoup.parse(driver.getPageSource());
                        doc.head().append(
                                "<script type=\"text/javascript\" src=\"http://livejs.com/live.js\"></script>\n");
                        FileOptions.writeToFileOverWrite(System.getProperty("user.dir") + "/testpage.html", doc.toString());
                        Thread.sleep(2000);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
            });
            e.shutdown();
        }
        int i = 0;
        boolean isChallenge = false;
        login(driver);
        while (true){
            //TODO add game
            try {
                //skip to end
                if((props.getProperty("skip.to.end") == null || props.getProperty("skip.to.end").toLowerCase().equals("true"))
                        && Jsoup.parse(driver.getPageSource()).select("div.battle-controls").select("i.fa-fast-forward").size() > 0)
                    driver.findElement(By.cssSelector(
                            Jsoup.parse(driver.getPageSource()).select("div.battle-controls").select("button").get(2).cssSelector()
                    )).click();
                if(!isChallenge && props.getProperty("challenge.user")!=null && !props.getProperty("challenge.user").isEmpty()) {
                    Elements e = Jsoup.parse(driver.getPageSource()).select("button.onlineonly");
                    if(e.size() > 2 && "finduser".equals(e.get(1).attr("name"))) {
                        try {
                            driver.findElement(By.cssSelector(
                                    e.get(1).cssSelector()
                            )).click(); //TODO exception here
                        }catch (Exception e1){
                            //do nothing
                        } // TODO: 6/13/18 focus punch is a bad move

                        Elements els = Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("input.textbox");
                        if(els.size() > 0) {
                            String ss = els.get(0).cssSelector();
                            ((ChromeDriver) driver).executeScript("document.querySelector('" + ss + "').value='" + props.getProperty("challenge.user") + "';");
                            String sr = Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("p.buttonbar").select("button").get(0).cssSelector();
                            driver.findElement(By.cssSelector(sr)).click();
                        }
                        Element challenge = Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("p.buttonbar").select("button").get(0);
                        if(challenge.hasAttr("disabled")){
                            driver.navigate().refresh();
                            return;
                        }
                        String srcr = challenge.cssSelector();
                        driver.findElement(By.cssSelector(srcr)).click();

                        String src = Jsoup.parse(driver.getPageSource()).select("form.battleform").select("p.buttonbar").select("button").get(0).cssSelector();
                        driver.findElement(By.cssSelector(src)).click();
                        isChallenge = true;
                    }
                }if(props.getProperty("find.random")!=null && props.getProperty("find.random").toLowerCase().equals("true") &&
                        !Jsoup.parse(driver.getPageSource()).select("form.battleform").select("button.mainmenu1").attr("style").equals("display: none;") &&
                        !Jsoup.parse(driver.getPageSource()).select("#room-").attr("style").contains("display: none;")) {
//                        && !Jsoup.parse(driver.getPageSource()).select("form.battleform").select("p.buttonbar").attr("style").equals(""))
                    driver.findElement(By.cssSelector(
//                            Jsoup.parse(driver.getPageSource()).select("form.battleform").select("p.buttonbar").get(0).cssSelector()
                            Jsoup.parse(driver.getPageSource()).select("form.battleform").select("button.mainmenu1").get(0).cssSelector()
                    )).click();
                }
                //accept all challenges
                if(!isChallenge && Jsoup.parse(driver.getPageSource()).select("p.buttonbar").select("button").size()>0 &&
                        !Jsoup.parse(driver.getPageSource()).select("p.buttonbar").get(0).attr("style").contains("display: none;") &&
                        !Jsoup.parse(driver.getPageSource()).select("#room-").attr("style").contains("display: none;")) {
                    driver.findElement(By.cssSelector(
                            Jsoup.parse(driver.getPageSource()).select("p.buttonbar").select("button").get(0).cssSelector()
                    )).click();
                }if(Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("p.buttonbar").select("button").text().trim().contains("Cancel"))
                    driver.findElement(By.cssSelector(
                        Jsoup.parse(driver.getPageSource()).select("div.ps-popup")
                                .select("p.buttonbar").select("button").get(1).cssSelector()
                    )).click();
                if (Jsoup.parse(driver.getPageSource()).select("div.battle-controls")
                        .text().contains("Rematch")) {
                    isChallenge = false;
                    //who won the match
                    Elements v = Jsoup.parse(driver.getPageSource()).select("div.battle-history");
                    String winner = v.get(v.size() - 1).text().trim();
                    //bot won
                    Elements trainers = Jsoup.parse(driver.getPageSource()).select("div.trainer > strong");
                    String oppsName = trainers.get(1).text().trim();
                    //might want this to be hsqldb but for now...
                    String destDir = System.getProperty("user.dir")+"/highscores";
//                    CloneCommand git = Git.cloneRepository()
//                            .setURI("https://github.com/unsupo/pokemonshowdownbot-highscores.git")
//                            .setDirectory(new File(destDir));
//                    String branch = "";
//                    if(!branch.isEmpty())
//                        git.setBranchesToClone(Collections.singleton(branch))
//                                .setCredentialsProvider(CredentialsProvider.getDefault())
//                                .setBranch(branch);
//                    Repository repository = git.getRepository();
//                    if(new File(destDir).exists()){
//                        repository = new FileRepositoryBuilder()
//                                .addCeilingDirectory(new File(destDir))
//                                .findGitDir(new File(destDir))
//                                .build();
//                        if(repository.getAllRefs().containsKey("HEAD"))
//                            org.eclipse.jgit.api.Git.wrap(repository).pull();
//                    }else
//                        git.call();
//                    new Git(repository).pull().call();
                    new File(destDir).mkdirs();
                    String ff = "winner_details.json", f = destDir+"/"+ff;
                    HashMap<String, WinnerDetails> winners = new HashMap<>();
                    if(new File(f).exists())
                        winners = new Gson().fromJson(FileOptions.readFileIntoString(f),new TypeToken<HashMap<String, WinnerDetails>>(){}.getType());
                    WinnerDetails winnerDetails = new WinnerDetails(oppsName);
                    if(winners.containsKey(oppsName))
                        winnerDetails = winners.get(oppsName);
                    else
                        winners.put(oppsName,winnerDetails);
                    if(winner.contains(userName))
                        winnerDetails.incrementLoss();
                    else
                        winnerDetails.incrementWin();
                    FileOptions.writeToFileOverWrite(f,new Gson().toJson(winners));
//                    new Git()
//                    new Git(repository).add().addFilepattern(ff).call();
//                    new Git(repository).commit().setMessage("updating file").call();
//                    new Git(repository).push().call();
                    // TODO: 6/13/18 check won/loss and if it contains userName if so then write bot won or loss and player x won/loss
                    // save your team with JSON.stringify(room.myPokemon)
                    // room.battle.activityQueue has all actions throughout battle
                    // JSON.stringify(room.battle.yourSide.pokemon) for you opponents mons which has moveTrack, but not all moves
                    //div.battle-history .get(size()-1)

                    driver.findElement(By.cssSelector(
                            Jsoup.parse(driver.getPageSource()).select("div.battle-controls")
                                    .select("div.controls").select("p").get(1).select("button").get(0).cssSelector()
                    )).click();
                }
                if (Jsoup.parse(driver.getPageSource()).select("div.ps-popup").text().contains("does not exist"))
                    driver.findElement(
                            By.cssSelector(
                                    Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("p.buttonbar").select("button").get(0).cssSelector()))
                            .click();
                if (Jsoup.parse(driver.getPageSource()).select("a.roomtab").select("span").text().trim().contains("(empty room)"))
                    driver.findElement(By.cssSelector(
                            Jsoup.parse(driver.getPageSource()).select("div.inner").select("button.closebutton")
                                    .get(0).cssSelector()
                    )).click();
                String whatDo = Jsoup.parse(driver.getPageSource()).select("div.whatdo").text().trim();
                if (whatDo.contains("What will") || whatDo.contains("Switch"))
                    try {
                        performAction(driver);
                    } catch (Exception e) {
                        if(e.getMessage().contains("unknown error: getAction is not defined"))
                            System.err.println("No javascript getAction method found");
                    }
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private static void performAction(WebDriver driver){
        if(props.getProperty("timer") == null || props.getProperty("timer").toLowerCase().equals("true")) {
            Elements timerButton = Jsoup.parse(driver.getPageSource()).select("div.whatdo").select("button.timerbutton");
            if (timerButton.text().trim().equals("Timer")) {
                driver.findElement(By.cssSelector(timerButton.get(0).cssSelector())).click();
                driver.findElement(By.cssSelector(
                        Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("button")
                                .get(0).cssSelector()
                )).click();
            }
        }
        //for outrage
        Elements switchmenu = Jsoup.parse(driver.getPageSource()).select("div.switchmenu").select("em");
        if(switchmenu.size() > 0 && switchmenu.get(0).text().trim().equals("You are trapped and cannot switch!")){
            driver.findElement(By.cssSelector(
                Jsoup.parse(driver.getPageSource()).select("div.movemenu").select("button").get(0).cssSelector()
            )).click();
            return;
        }
        String action = ((ChromeDriver)driver).executeScript("return getAction().description;").toString();
        String[] actions = action.split(": ");
        try {
            ((ChromeDriver) driver).executeScript("$('input[name=megaevo]')[0].checked=true;");
        }catch (Exception e){/*always try to mega even if it's not a mega*/}

        Elements buttons = Jsoup.parse(driver.getPageSource()).select("div.whatdo").select("div.movemenu").select("button");
        boolean active = ((ChromeDriver) driver).executeScript("return getAction().mon.active;").toString().equals("true");
        String whatDo = Jsoup.parse(driver.getPageSource()).select("div.whatdo").text().trim();
        if(whatDo.contains("Switch"))
            active = false;
        if(active){
            String selector = "";
            for(Element e : Jsoup.parse(driver.getPageSource()).select("div.movemenu").select("button"))
                if(e.attr("data-move").contains(actions[1])){
                    selector = e.cssSelector();
                    break;
                }
            driver.findElement(By.cssSelector(selector)).click();
        }else {
            String selector = "";
            for(Element e : Jsoup.parse(driver.getPageSource()).select("div.switchmenu").select("button"))
                if(actions[0].contains(e.text().trim().replace("\"",""))){
                    selector = e.cssSelector();
                    break;
                }
            driver.findElement(By.cssSelector(selector)).click();
        }
    }

    public static String userName;

    private static void login(WebDriver driver){
        crossOrigin(driver);
        //login
        String bs = Jsoup.parse(driver.getPageSource()).select("div.userbar").select("button").get(0).cssSelector();
        driver.findElement(By.cssSelector(bs)).click();
        String ss = Jsoup.parse(driver.getPageSource()).select("input.textbox").get(0).cssSelector();
        new WebDriverWait(driver,10000).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(ss)));
        // TODO: 6/12/18 UID new user or use temp password
        String username = "d45cda786aa947d0ba";//UUID.randomUUID().toString().replace("-","").substring(0,18);
        if(replacer(props.getProperty("username"))!=null)
            username = replacer(props.getProperty("username"));
        userName = username;
        ((ChromeDriver) driver).executeScript("document.querySelector('"+ss+"').value='"+username+"';");
        String sb = Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("p.buttonbar").select("button").get(0).cssSelector();
        driver.findElement(By.cssSelector(sb)).click();
        crossOrigin(driver);
        Elements p = Jsoup.parse(driver.getPageSource()).select("div.ps-popup");
        if(p.select("p.error").text().trim().equals("The name you chose is registered.")) {
            String ssl = p.select("input.textbox").get(0).cssSelector();
            String password = "password";
            if(props.getProperty("password")!=null)
                password = props.getProperty("password");
            ((ChromeDriver) driver).executeScript("document.querySelector('"+ssl+"').value='"+password+"';");
            driver.findElement(By.cssSelector(p.select("p.buttonbar").select("button").get(0).cssSelector())).click();
            crossOrigin(driver);
        }
    }

    private static String replacer(String s) {
        if(s==null)
            return s;
        if(s.contains("${uuid}"))
            s = s.replace("${uuid}",UUID.randomUUID().toString().replace("-","").substring(0,18));
        if(s.contains("${user.dir}"))
            s = s.replace("${uuid}",System.getProperty("user.dir"));
        return s;
    }

    private static void crossOrigin(WebDriver driver){
        //dumb cross origin
        new WebDriverWait(driver,10000).until(ExpectedConditions.visibilityOfElementLocated(By.id("overlay_iframe")));
        driver.switchTo().frame(((ChromeDriver) driver).findElementById("overlay_iframe")).getPageSource();
        try {
            if (Jsoup.parse(driver.getPageSource()).select("input").size() >= 4) {
                Elements p = Jsoup.parse(driver.getPageSource()).select("input");
                driver.findElement(By.cssSelector(
                        p.get(p.size() - 1).cssSelector()
                )).click();
            }
        }catch (Exception aie){
            if (Jsoup.parse(driver.getPageSource()).select("input").size() >= 4) {
                Elements p = Jsoup.parse(driver.getPageSource()).select("input");
                driver.findElement(By.cssSelector(
                        p.get(p.size() - 1).cssSelector()
                )).click();
            }
        }
        new WebDriverWait(driver,10000).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("pre")));
        String text = Jsoup.parse(((ChromeDriver) driver).executeScript("return document.body.innerHTML;").toString()).select("pre").text();
        driver.switchTo().parentFrame();
        String selector = Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("input").get(0).cssSelector();
        ((ChromeDriver) driver).executeScript("document.querySelector('"+selector+"').value='"+text+"';");
//        driver.findElement(By.cssSelector(selector)).sendKeys(text);
        String buttonSelector = Jsoup.parse(driver.getPageSource()).select("div.ps-popup").select("button").get(0).cssSelector();
        driver.findElement(By.cssSelector(buttonSelector)).submit();
    }

    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
