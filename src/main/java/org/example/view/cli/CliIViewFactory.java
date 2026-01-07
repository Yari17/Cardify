package org.example.view.cli;

import org.example.view.ILoginView;
import org.example.view.IRegistrationView;
import org.example.view.IViewFactory;

public class CliIViewFactory implements IViewFactory {

    @Override
    public ILoginView createLoginView() {
        return new CliILoginView();
    }

    @Override
    public IRegistrationView createRegistrationView() {
        return null;
    }
}