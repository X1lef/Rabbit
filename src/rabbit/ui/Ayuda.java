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

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Ayuda extends JDialog {
    private JEditorPane editorPane;

    public Ayuda (JFrame frame) {
        super (frame, "Ayuda");
        setModal(true);
        setSize(900, 600);
        setLayout(new GridLayout(1,1));
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        try {
            editorPane = new JEditorPane(getClass().getResource("documentacion.html"));
            editorPane.setEditable(false);
            editorPane.setContentType("text/html");

        } catch (IOException e) {
            e.printStackTrace();
        }

        add(new JScrollPane(editorPane));

        setVisible(true);
    }
}
