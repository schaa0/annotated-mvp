package de.hda.simple_example.di;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.model.Repository;

@Module
public class ModuleRepository {

    private Repository repository;

    public ModuleRepository(Repository repository){
        this.repository = repository;
    }

    @Provides
    public Repository getRepository() {
        return repository;
    }

    @Provides
    public String aString(){
        return "";
    }
}
