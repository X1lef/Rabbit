/*
 * Copyright (C) 2017 Félix Pedrozo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package rabbit.ui;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import rabbit.io.ConfDeUsuario;
import rabbit.io.LeerArchivo;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;

import static rabbit.io.ConfDeUsuario.*;

public class EditorDeTexto extends JPanel {
    private File file;

    private RSyntaxTextArea textArea;
    private RTextScrollPane scroll;

    private JPanel jpEditor;
    private JPanel jpPosicionCursor;

    private EditorUI editorUI;

    static int fontSize;

    static {
        //Se obtiene el tamaño de la fuente guardado.
        fontSize = ConfDeUsuario.getInt(KEY_FUENTE_TAMANIO);
    }

    EditorDeTexto (File file, EditorUI editorUI) {
        this (file, 0, editorUI);
    }

    EditorDeTexto(File file, int poscCursor, final EditorUI editorUI) {
        setLayout(new GridBagLayout());

        this.editorUI = editorUI;
        this.file = file;

        textArea = new RSyntaxTextArea();
        textArea.setTabSize(4);
        textArea.setFocusable(true);
        textArea.setMarkOccurrences(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.setClearWhitespaceLinesEnabled(false);

        FoldParserManager.get().addFoldParserMapping("text/sl", new CurlyFoldParser());

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/sl", "rabbit.ui.SLTokenMaker");
        textArea.setSyntaxEditingStyle("text/sl");
        textArea.setPaintTabLines(ConfDeUsuario.getBoolean(KEY_GUIAS_IDENTACION));

        InputMap map = textArea.getInputMap();
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
        String actionName = RSyntaxTextAreaEditorKit.rstaPossiblyInsertTemplateAction;
        map.put(ks, actionName);

        textArea.setText(LeerArchivo.leer(file));
        textArea.setCaretPosition(poscCursor);
        textArea.requestFocus();

        textArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                actualizarPosCursor(textArea.getCaretPosition());
                EditorDeTexto.this.editorUI.actualizarMenuItem (textArea.canUndo(), textArea.canRedo());
            }
        });

        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                EditorDeTexto.this.editorUI.actualizarMenuItem (textArea.canUndo(), textArea.canRedo());
            }
        });

        confPanelEditor();
        actualizarTema(ConfDeUsuario.getString(KEY_TEMA));
        confPanelPosicionCursor ();
        actualizarPosCursor(textArea.getCaretPosition());

        RSyntaxTextArea.setTemplatesEnabled(true);
        CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();

        //Se añaden las plantillas de código.
        for (CodeTemplate ct : getCodeTemplate()) {
            ctm.addTemplate(ct);
        }

        GridBagConstraints conf = new GridBagConstraints();

        //Configuración del componente en la fila 0 columna 0.
        conf.gridx = conf.gridy = 0;
        conf.weightx = conf.weighty = 1.0;
        conf.fill = GridBagConstraints.BOTH;

        add(jpEditor, conf);

        //Configuración del componente en la fila 1 columna 0.
        conf.gridy = 1;
        conf.weighty = 0.0;
        conf.fill = GridBagConstraints.HORIZONTAL;

        add (jpPosicionCursor, conf);
    }

    private void actualizarPosCursor (int caretPosc) {
        //Se extrae el label que contiene el panel 'jpPosicionCursor'.
        JLabel jlPosCursor = (JLabel) jpPosicionCursor.getComponent(1);

        try {
            int fila = textArea.getLineOfOffset(caretPosc);
            int colum = caretPosc - textArea.getLineStartOffset(fila);

            fila ++;
            colum ++;

            jlPosCursor.setText(fila + " : " + colum);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void confPanelEditor () {
        scroll = new RTextScrollPane(textArea);
        scroll.getGutter().setBorder(new Gutter.GutterBorder(0, 0, 0, 5));
        scroll.getGutter().setLayout(new BorderLayout(10, 0));
        scroll.setFoldIndicatorEnabled(true);
        scroll.setLineNumbersEnabled(ConfDeUsuario.getBoolean(KEY_NUM_LINEA));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        jpEditor  = new JPanel(new GridLayout(1, 1));
        jpEditor.add(scroll);
    }

    private void confPanelPosicionCursor () {
        jpPosicionCursor = new JPanel();
        jpPosicionCursor.setLayout(new BoxLayout(jpPosicionCursor, BoxLayout.X_AXIS));

        jpPosicionCursor.add(Box.createHorizontalGlue());
        jpPosicionCursor.add(new JLabel());
        jpPosicionCursor.add(Box.createRigidArea(new Dimension(10, 18)));
    }

    void copiar () {
        textArea.copy();
    }

    void cortar () {
        textArea.cut();
    }

    void pegar () {
        textArea.paste();
    }

    void seleccTodo () {
        textArea.selectAll();
    }

    File getFile() {
        return file;
    }

    void setFile(File file) {
        this.file = file;
    }

    void setText(String text) {
        int caretPos = 0;

        //Compruebo si el archivo va ha ser recargado para guardar la posición del cursor.
        if (text.length() >= textArea.getText().length())
            caretPos = textArea.getCaretPosition();

        textArea.setText(text);
        textArea.setCaretPosition(caretPos);
    }

    String getText() {
        return textArea.getText();
    }

    void rehacer() {
        textArea.redoLastAction();
    }

    void deshacer() {
        textArea.undoLastAction();
    }

    public String toString () {
        return file.getName();
    }

    public boolean equals (Object o) {
        if (o instanceof EditorDeTexto) {
            if (file.getAbsolutePath().equals(((EditorDeTexto)o).getFile().getAbsolutePath()))
                return true;
        }

        return false;
    }

    public int hashCode () {
        return file.getAbsolutePath().hashCode();
    }

    void habilitarNumLineas(boolean state) {
        scroll.setLineNumbersEnabled(state);
    }

    void habilitarGuiasDeIdentacion(boolean state) {
        textArea.setPaintTabLines(state);
    }

    boolean archivoModificado() {
        String textGuardado = LeerArchivo.leer(file);

        return !textGuardado.equals(textArea.getText());
    }

    String archivoModifRetornaContenido() {
        String textGuardado = LeerArchivo.leer(file);
        String textEditor = textArea.getText();

        if (textGuardado.equals(textEditor)) return null;

        return textGuardado;
    }

    void actualizarTema(String nombreTema) {
        try {
            InputStream in = getClass().getResourceAsStream("/rabbit/theme/" + nombreTema + ".xml");
            Theme theme = Theme.load(in);
            theme.apply(textArea);
            actualizarFuente();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void actualizarFuente() {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);

        textArea.setFont(font);
        scroll.getGutter().setLineNumberFont(font);
    }

    JComponent getComponent () {
        return textArea;
    }

    private CodeTemplate[] getCodeTemplate() {
        CodeTemplate[] ct = {
                new StaticCodeTemplate("dh", "desde ", " = 1 hasta 10 {\n\t\n}"),
                new StaticCodeTemplate("rh", "repetir\n\t\nhasta (", ")"),
                new StaticCodeTemplate("m", "mientras (", ") {\n\t\n}"),
                new StaticCodeTemplate("i", "imprimir ('", "')"),
                new StaticCodeTemplate("l", "leer (", ")"),
                new StaticCodeTemplate("c", "cls()", null),
                new StaticCodeTemplate("if", "inicio\n\t", "\nfin"),
                new StaticCodeTemplate("s", "si (", ") {\n\t\n}"),
                new StaticCodeTemplate("ss", "si (", ") {\n\t\nsino\n\t\n}"),
                new StaticCodeTemplate("sss", "si (", ") {\n\t\nsino si ()\n\t\n}"),
                new StaticCodeTemplate("ec", "eval {\n\tcaso (", ")\n\t\t\n}"),
                new StaticCodeTemplate("vif", "var\n\t", "\ninicio\n\t\nfin"),
                new StaticCodeTemplate("tif", "tipos\n\t", "\ninicio\n\t\nfin"),
                new StaticCodeTemplate("cif", "const\n\t", "\ninicio\n\t\nfin"),
                new StaticCodeTemplate("srif", "sub ", " () retorna \ninicio\n\t\nfin"),
                new StaticCodeTemplate("sif", "sub ", " ()\ninicio\n\t\nfin")
        };

        return ct;
    }
}
