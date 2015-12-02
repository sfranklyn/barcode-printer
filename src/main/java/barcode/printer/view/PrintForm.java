/*
 * Copyright 2015 Samuel Franklyn <sfranklyn at gmail.com>.
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.CodaBarWriter;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.oned.ITFWriter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.ISpannableGridRow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 *
 * @author Samuel Franklyn <sfranklyn at gmail.com>
 */
@Singleton
public class PrintForm extends BaseForm {

    private static final long serialVersionUID = -394447964597969311L;
    private static final Logger LOGGER = Logger.getLogger(PrintForm.class.getName());
    private JComboBox comboCode;
    private JTextField textBarCode;
    private JButton buttonAdd;
    private JButton buttonUpdate;
    private JButton buttonDelete;
    private JButton buttonPrint;
    private DefaultListModel<String> listModel;
    private JScrollPane scrollPaneBarCode;
    private JList listBarCode;

    @PostConstruct
    public void init() {
        setTitle("Bar Code Printer");

        JPanel panel = new JPanel();
        DesignGridLayout layout = new DesignGridLayout(panel);

        String[] codeStrings = {"CodaBar", "Code 128", "Code 39",
            "EAN-13", "EAN-8", "ITF"};
        comboCode = new JComboBox<>(codeStrings);
        textBarCode = new JTextField("", 15);

        buttonAdd = new JButton("Add");
        buttonAdd.setMnemonic(KeyEvent.VK_A);
        buttonAdd.setActionCommand("Add");
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonAddAction();
            }
        });

        buttonUpdate = new JButton("Update");
        buttonUpdate.setMnemonic(KeyEvent.VK_U);
        buttonUpdate.setActionCommand("Update");
        buttonUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonUpdateAction();
            }
        });

        buttonDelete = new JButton("Delete");
        buttonDelete.setMnemonic(KeyEvent.VK_D);
        buttonDelete.setActionCommand("Delete");
        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonDeleteAction();
            }
        });
        buttonDelete.setEnabled(false);

        buttonPrint = new JButton("Print");
        buttonPrint.setMnemonic(KeyEvent.VK_P);
        buttonPrint.setActionCommand("Print");
        buttonPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPrintAction();
            }
        });

        listModel = new DefaultListModel<>();

        listBarCode = new JList(listModel);
        listBarCode.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneBarCode = new JScrollPane();
        scrollPaneBarCode.getViewport().setView(listBarCode);
        //scrollPaneBarCode.setSize(1, 100);

        layout.row().grid(new JLabel("Code")).add(comboCode);
        layout.row().grid(new JLabel("Bar Code")).add(textBarCode);
        ISpannableGridRow row = layout.row().grid().add(buttonAdd);
        row.add(buttonUpdate);
        row.add(buttonDelete);
        row.add(buttonPrint);
        layout.row().grid().add(scrollPaneBarCode);

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    public void buttonAddAction() {
        listModel.addElement(textBarCode.getText());
        if (listModel.getSize() > 0) {
            buttonDelete.setEnabled(true);
        }
        textBarCode.setText("");
    }

    public void buttonUpdateAction() {
        int index = listBarCode.getSelectedIndex();
        if (index < 0) {
            return;
        }
        listModel.set(index, textBarCode.getText());
    }

    public void buttonDeleteAction() {
        int index = listBarCode.getSelectedIndex();
        if (index < 0) {
            return;
        }
        listModel.remove(index);

        int size = listModel.getSize();

        if (size == 0) { //Nobody's left, disable firing.
            buttonDelete.setEnabled(false);

        } else { //Select an index.
            if (index == listModel.getSize()) {
                //removed item in last position
                index--;
            }

            listBarCode.setSelectedIndex(index);
            listBarCode.ensureIndexIsVisible(index);
        }
        if (listModel.getSize() <= 0) {
            buttonDelete.setEnabled(false);
        }
    }

    public void buttonPrintAction() {
        if (listModel.isEmpty()) {
            return;
        }
        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();

            document.addPage(page);

            PDFont font = PDType1Font.COURIER_BOLD;

            float fontSize = 10f;
            float fontWidth = font.getStringWidth("W") / 1000 * fontSize;
            float fontHeight = font.getFontDescriptor().getFontBoundingBox().
                    getHeight() / 1000 * fontSize;
            float startXOrigin = fontWidth * 0.3f;
            float startYOrigin = page.findMediaBox().getUpperRightY();
            float startX = startXOrigin;
            float startY;

            try (PDPageContentStream cs
                    = new PDPageContentStream(document, page)) {

                int bcWidth = 80;
                int bcHeight = 20;

                int row = 1;

                for (int idx = 0; idx < listModel.getSize(); idx++) {
                    BitMatrix bitMatrix;
                    String strBarCode = listModel.get(idx);
                    switch (comboCode.getSelectedIndex()) {
                        case 0:
                            bitMatrix = new CodaBarWriter().
                                    encode(strBarCode, BarcodeFormat.CODABAR, bcWidth, bcHeight);
                            break;
                        case 1:
                            bitMatrix = new Code128Writer().
                                    encode(strBarCode, BarcodeFormat.CODE_128, bcWidth, bcHeight);
                            break;
                        case 2:
                            bitMatrix = new Code39Writer().
                                    encode(strBarCode, BarcodeFormat.CODE_39, bcWidth, bcHeight);
                            break;
                        case 3:
                            bitMatrix = new EAN13Writer().
                                    encode(strBarCode, BarcodeFormat.EAN_13, bcWidth, bcHeight);
                            break;
                        case 4:
                            bitMatrix = new EAN8Writer().
                                    encode(strBarCode, BarcodeFormat.EAN_8, bcWidth, bcHeight);
                            break;
                        case 5:
                            bitMatrix = new ITFWriter().
                                    encode(strBarCode, BarcodeFormat.ITF, bcWidth, bcHeight);
                            break;
                        default:
                            bitMatrix = new CodaBarWriter().
                                    encode(strBarCode, BarcodeFormat.CODABAR, bcWidth, bcHeight);
                            break;
                    }

                    BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                    PDXObjectImage pdxoi = new PDJpeg(document, bufferedImage);

                    startY = startYOrigin - (bitMatrix.getHeight() * row * 1.6f);
                    row++;

                    cs.beginText();
                    cs.setFont(font, fontSize);
                    cs.moveTextPositionByAmount(startX + fontWidth, startY + fontHeight);
                    cs.drawString(strBarCode);
                    cs.endText();
                    cs.drawImage(pdxoi, startX, startY - fontHeight);
                }

            }

            document.silentPrint();

            listModel.clear();
            buttonDelete.setEnabled(false);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            LOGGER.log(Level.SEVERE, null, ex);
        }

    }

}
