package com.poc.hub.features.contacts;

import com.poc.hub.core.feature.FeatureDescriptor;

import org.springframework.stereotype.Component;

@Component
class ContactsFeature implements FeatureDescriptor {

    private final ContactRepository repository;

    ContactsFeature(ContactRepository repository) {
        this.repository = repository;
    }

    @Override
    public String key() {
        return "contacts";
    }

    @Override
    public String name() {
        return "Contacts";
    }

    @Override
    public String description() {
        return "Keep an address book you can search.";
    }

    @Override
    public String route() {
        return "/contacts";
    }

    @Override
    public String apiPath() {
        return "/api/contacts";
    }

    @Override
    public int phase() {
        return 3;
    }

    @Override
    public int order() {
        return 60;
    }

    @Override
    public long itemCount() {
        return repository.count();
    }
}
