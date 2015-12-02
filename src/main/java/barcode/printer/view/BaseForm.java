/*
 * Copyright 2015 Samuel Franklyn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package barcode.printer.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;

/**
 *
 * @author Samuel Franklyn <sfranklyn at gmail.com>
 */
public abstract class BaseForm extends JFrame {

    private static final long serialVersionUID = -7658994021188468346L;

    public BaseForm() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                windowClosingAction();
            }
        });
    }

    public void windowClosingAction() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.shutdown();
        dispose();
        System.exit(0);
    }
}
