package de.hda.simple_example;

import com.mvp.IMvpEventBus;
import com.mvp.MockablePresenterComponent;
import com.mvp.MvpEventBus;
import com.mvp.PresenterComponent;

import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.container.IView;
import de.hda.simple_example.container.MainActivity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Andy on 02.12.2016.
 */

public class MockableMainPresenterComponent extends MockablePresenterComponent<IView, MainPresenter> {

    public MockableMainPresenterComponent(IView view) {
        super(view);
    }

    @Override
    public MainPresenter newInstance() {
        MainPresenter mockPresenter = mock(MainPresenter.class);
        when(mockPresenter.getView()).thenReturn(view());
        return mockPresenter;
    }

}
