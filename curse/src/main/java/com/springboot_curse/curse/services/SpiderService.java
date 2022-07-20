package com.springboot_curse.curse.services;

import com.springboot_curse.curse.entities.WebPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
public class SpiderService {
    @Autowired
    private SearchService searchService;
    public void indexWebPages()
    {
        List<WebPage>linksToIndex=searchService.getLinksToIndex();
        linksToIndex.stream().parallel().forEach(webPage->{
            try {
                indexWebPage(webPage);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        });
    }

    private void indexWebPage(WebPage webPage) throws Exception {
        String url= webPage.getUrl();
        String content=getWebContent(url);
        if(isBlank(content)){
            return;
        }

        indexAndSaveWebPage(webPage, content);
        saveLinks(getDomain(url),content);
    }

    private String getDomain(String url) {
        String[] aux=url.split("/");
        return aux[0] + "//" + aux[2];
    }

    private void saveLinks(String domain,String content){
        List<String> links = getLinks(domain,content);
        links.stream().filter(link-> !searchService.exist(link))
                .map(link -> new WebPage(link))
                .forEach(webPage-> searchService.save(webPage));
    }
    public List<String> getLinks(String domain,String content)
    {
        List<String> links=new ArrayList<>();
        String[] splitHref = content.split("href=\"");

        List<String> listHref = Arrays.asList(splitHref);
        //listHref.remove(0);
        listHref.forEach(strHref -> {
            String[] aux = strHref.split("\"");
            links.add(aux[0]);
        });
        return cleanLinks(domain,links);
    }
    private List<String> cleanLinks(String domain ,List<String>links)
    {
        String[] excludedExtensions = new String[]{"css","js","json","jpg","png","jpeg"};
        List<String>resultLinks=links.stream()
                .filter(link-> Arrays.stream(excludedExtensions)
                .noneMatch(link::endsWith))
                .map(link -> link.startsWith("/")?domain+link:link)
                .collect(Collectors.toList());

        List<String> uniqueLinks=new ArrayList<String>();
        uniqueLinks.addAll(new HashSet<String>(resultLinks));
        return uniqueLinks;
    }
    private void indexAndSaveWebPage(WebPage webPage, String content) {
        String title=getTitle(content);
        String description=getDescription(content);
        webPage.setTitle(title);
        webPage.setDescription(description);
        searchService.save(webPage);
    }

    public String getTitle(String content)
    {
        String[] aux = content.split("<title>");
        String[] aux2 = aux[1].split("</title>");
        return aux2[0];
    }
    public String getDescription(String content) {
        String[] aux = content.split("<meta name=\"description\" content=\"");
        String[] aux2 = aux[1].split("\">");
        return aux2[0];
    }
    public synchronized String getWebContent(String link){
        System.out.println("INIT");
        System.out.println(link);
        try{
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String encoding=conn.getContentEncoding();

            InputStream input=conn.getInputStream();
            System.out.println("END");
            return new BufferedReader(new InputStreamReader(input))
                    .lines()
                    .collect(Collectors.joining());
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
