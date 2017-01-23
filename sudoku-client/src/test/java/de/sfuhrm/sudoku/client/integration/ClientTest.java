/*
Sudoku - a fast Java Sudoku game creation library.
Copyright (C) 2017  Stephan Fuhrmann

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
Boston, MA  02110-1301, USA.
*/
package de.sfuhrm.sudoku.client.integration;

import de.sfuhrm.sudoku.GameMatrix;
import de.sfuhrm.sudoku.client.Client;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.kohsuke.args4j.CmdLineException;

/**
 * Test for {@link Client}.
 * @author Stephan Fuhrmann
 */
public class ClientTest {

    @Test
    public void testMain() throws CmdLineException, UnsupportedEncodingException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(arrayOutputStream));
        Client.main(new String[] {});
        String output = arrayOutputStream.toString("UTF-8");
        output = output.trim();
        GameMatrix matrix = new GameMatrix();
        matrix.setAll(GameMatrix.parse(output.split("\n")));
        assertEquals(true, matrix.isValid());
        assertEquals(9*9, matrix.getSetCount());
    }
    
    @Test
    public void testMainWithRiddle() throws CmdLineException, UnsupportedEncodingException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(arrayOutputStream));
        Client.main(new String[] {"-e", "Riddle"});
        String output = arrayOutputStream.toString("UTF-8");
        output = output.trim();
        GameMatrix matrix = new GameMatrix();
        matrix.setAll(GameMatrix.parse(output.split("\n")));
        assertEquals(true, matrix.isValid());
    }    
}