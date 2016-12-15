package de.hda.simple_example.container;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;

import java.io.IOException;
import java.util.List;

import de.hda.simple_example.business.ExamplePresenter;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 13.12.2016.
 */

@UIView(presenter = ExamplePresenter.class)
public class ExampleActivity extends AppCompatActivity implements IView{

     @Presenter
     ExamplePresenter examplePresenter;

     @Override
     protected void onCreate(@Nullable Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
     }

     @Override
     protected void onStart() {
          super.onStart();
          examplePresenter.loadThings();
     }

     @Override
     public void showError(IOException e) {

     }

     @Override
     public void setUp() {

     }

     @Override
     public void showProgressBar() {

     }

     @Override
     public void hideProgressBar() {

     }

     @Override
     public boolean isLoading() {
          return false;
     }

     @Override
     public void setRepositories(List<Repository> repositories) {

     }

     @Override
     public void addRepositories(List<Repository> repositories) {

     }

     @Override
     public void showToast(Repository repository) {

     }

     @Override
     public void showToast(Location location) {

     }
}
