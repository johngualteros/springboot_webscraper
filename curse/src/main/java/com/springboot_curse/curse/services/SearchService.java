package com.springboot_curse.curse.services;

import com.springboot_curse.curse.entities.WebPage;
import com.springboot_curse.curse.repositories.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private SearchRepository repository;

    public List<WebPage> search(String textSearch) {
        return repository.search(textSearch);
    }

    public void save(WebPage webPage) {
        repository.save(webPage);
    }

    public boolean exist(String link)
    {
        return repository.exist(link);
    }
    public List<WebPage> getLinksToIndex()
    {
        return repository.getLinksToIndex();
    }

}
