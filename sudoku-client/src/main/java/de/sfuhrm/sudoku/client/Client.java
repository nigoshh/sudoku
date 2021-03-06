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
package de.sfuhrm.sudoku.client;

import de.sfuhrm.sudoku.Creator;
import de.sfuhrm.sudoku.GameMatrix;
import de.sfuhrm.sudoku.Riddle;
import de.sfuhrm.sudoku.Solver;
import de.sfuhrm.sudoku.output.GameMatrixFormatter;
import de.sfuhrm.sudoku.output.LatexTableFormatter;
import de.sfuhrm.sudoku.output.MarkdownTableFormatter;
import de.sfuhrm.sudoku.output.PlainTextFormatter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * A Sudoku CLI client.
 * @author Stephan Fuhrmann
 */
public class Client {

    @Option(name = "-n", aliases = {"-count"}, usage = "The number of outputs to create")
    private int count = 1;
    enum Op {
        Full,
        Riddle,
        Both,
        Solve
    };
    
    enum Formatter {
        PlainText(PlainTextFormatter.class),
        MarkDownTable(MarkdownTableFormatter.class),
        LatexTable(LatexTableFormatter.class)
        ;

        private final Class<? extends GameMatrixFormatter> clazz;
        private Formatter(Class<? extends GameMatrixFormatter> inClazz) {
            this.clazz = inClazz;
        }
        
        public GameMatrixFormatter newInstance() {
            try {
                return clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new IllegalStateException(ex);
            }
        }
    };
    
    @Option(name = "-f", aliases = {"-format"}, usage = "The output format to use")
    private Formatter format = Formatter.PlainText;
    
    @Option(name = "-e", aliases = {"-exec"}, usage = "The operation to perform")
    private Op op = Op.Full;
    
    @Option(name = "-t", aliases = {"-time"}, usage = "Show timing information")
    private boolean timing;
    
    @Option(name = "-q", aliases = {"-quiet"}, usage = "No output")
    private boolean quiet;
    
    @Option(name = "-i", aliases = {"-input"}, usage = "Input file to read for solving")
    private Path input;
    
    @Option(name = "-h", aliases = {"-help"}, usage = "Show this command line help")
    private boolean help;
    
    private void solve(GameMatrixFormatter formatter) throws FileNotFoundException, IOException {
        if (op == Op.Solve && input == null) {
            throw new IllegalArgumentException("Expecting input file for Solve");
        }

        List<String> lines = Files.readAllLines(input);
        lines.stream()
                .filter(l -> !l.isEmpty())
                .map(l -> l.replaceAll("[_?.]", "0"))
                .collect(Collectors.toList());
        
        byte[][] data = GameMatrix.parse(lines.toArray(new String[0]));
        
        Riddle riddle = new Riddle();
        riddle.setAll(data);
        Solver solver = new Solver(riddle);
        List<Riddle> sollutions = solver.solve();
        if (!quiet) {
            for (Riddle r : sollutions) {
                System.out.println(formatter.format(r));
            }
        }
    }
    
    private void run() throws IOException {
        GameMatrixFormatter formatter = format.newInstance();
        long start = System.currentTimeMillis();
        
        if (!quiet) {
            System.out.print(formatter.documentStart());
        }
        
        if (op == Op.Solve) {
            solve(formatter);
        } else {
            for (int i = 0; i < count; i++) {
                switch (op) {
                    case Full: {
                        GameMatrix matrix = Creator.createFull();
                        if (!quiet) {
                            System.out.print(formatter.format(matrix));
                        }
                        break;
                    }
                    case Riddle: {
                        GameMatrix matrix = Creator.createFull();
                        Riddle riddle = Creator.createRiddle(matrix);
                        if (!quiet) {
                            System.out.print(formatter.format(riddle));
                        }
                        break;
                    }
                    case Both: {
                        GameMatrix matrix = Creator.createFull();
                        Riddle riddle = Creator.createRiddle(matrix);
                        if (!quiet) {
                            System.out.print(formatter.format(riddle));
                        }
                        if (!quiet) {
                            System.out.print(formatter.format(matrix));
                        }
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unhandled case "+op);
                }
            }
        }
        long end = System.currentTimeMillis();
        if (!quiet) {
            System.out.print(formatter.documentEnd());
        }
        
        
        if (timing) {
            System.err.println("Took total of "+(end-start)+"ms");
            System.err.println("Each iteration took "+(end-start)/count+"ms");
        }
    }
    
    public static void main(String[] args) throws CmdLineException, IOException {
        Client client = new Client();
        CmdLineParser parser = new CmdLineParser(client);
        parser.parseArgument(args);
        if (client.help) {
            parser.printUsage(System.out);
            return;
        }
        
        client.run();
    }
}
