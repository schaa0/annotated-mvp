package de.hda.simple_example.container;

import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mvp.annotation.ModuleParam;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import de.hda.simple_example.business.ExamplePresenter;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 13.12.2016.
 */

@UIView(presenter = ExamplePresenter.class)
public class ExampleActivity extends AppCompatActivity implements IExampleView{


     @Presenter ExamplePresenter examplePresenter;
     @ModuleParam private Repository repository;
     @ModuleParam private RepositoryAdapter savedState;


     @Override
     protected void onCreate(@Nullable Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          repository = getIntent().getParcelableExtra("repository");
          savedState = new RepositoryAdapter();
     }

     @Override
     protected void onStart() {
          super.onStart();
          examplePresenter.showRepositoryId();
     }


     @Override
     public void showRepository(Repository repository) {
          Toast.makeText(this, String.valueOf(repository.getId()), Toast.LENGTH_SHORT).show();
     }
}
