package de.hda.simple_example.inject;

import android.support.annotation.Nullable;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.container.RepositoryAdapter;
import de.hda.simple_example.container.SavedState;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 17.12.2016.
 */

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

}
