package com.rivage;

import java.util.List;

/**
 * <p>In the video game Rivage there is a puzzle that requires a combination of fuses that satisfies all breakers.</p>
 * We are given a set of colored fuses that must fit into breaker sockets.<br/>
 * We are given a set of breakers that have three sockets each.  Each breaker also subtracts up to two colors from the
 * fuses and presents a match condition.<br/>
 * There are not enough fuses for every socket.<br/>
 * Fuse colors follow rules:
 * <ul>
 * <li>red + blue = pink,</li>
 * <li>red + green = yellow,</li>
 * <li>green + blue = cyan,</li>
 * <li>red + blue + green = white</li>
 * </ul>
 * All other combinations would stack: 1red+2red = 3 red<br/>
 * Given the limited number of fuses, find the combination that satisfies all breakers.
 */
public class RivageSolver1 {
    static class Color {
        final String name;
        int r,g,b;

        public Color() {
            name="";
        }

        public Color(int r, int g, int b, String name) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        static final Color RED    = new Color(1, 0, 0, "Red"    );
        static final Color BLUE   = new Color(0, 0, 1, "Blue"   );
        static final Color GREEN  = new Color(0, 1, 0, "Green"  );
        static final Color PINK   = new Color(1, 0, 1, "Pink"   );
        static final Color YELLOW = new Color(1, 1, 0, "Yellow" );
        static final Color CYAN   = new Color(0, 1, 1, "Cyan"   );
        static final Color WHITE  = new Color(1, 1, 1, "White"  );
        static final Color BLACK  = new Color(0, 0, 0, "Black"  );
    }

    // existing fuses, some colors are repeated to allow for multiple uses.
    // To satisfy breakers that have less than three filled sockets, the fuses list has been padded with black fuses.
    static final Color [] fuses = new Color[]{
            Color.RED,            Color.RED,            Color.RED,
            Color.RED,            Color.RED,            Color.BLUE,
            Color.BLUE,           Color.BLUE,           Color.BLUE,
            Color.GREEN,          Color.GREEN,          Color.GREEN,
            Color.GREEN,          Color.PINK,           Color.PINK,
            Color.PINK,           Color.YELLOW,         Color.YELLOW,
            Color.CYAN,           Color.CYAN,           Color.CYAN,
            Color.WHITE,          Color.BLACK,          Color.BLACK,
            Color.BLACK,          Color.BLACK,          Color.BLACK,
            Color.BLACK,          Color.BLACK,          Color.BLACK,
    };

    static class Breaker {
        int [] sockets = new int[3];
        List<Color> minus;
        Color satisfaction;

        public Breaker(Color satisfaction,List<Color> minus) {
            this.satisfaction = satisfaction;
            this.minus = minus;
            sockets[0] = sockets[1] = sockets[2] = -1;
        }

        public boolean isSatisfied(final List<Integer> fuseIndices) {
            Color sum = new Color();
            for (Integer index : fuseIndices) {
                Color fuse = fuses[index];
                sum.r += fuse.r;
                sum.g += fuse.g;
                sum.b += fuse.b;
            }
            for (Color minusColor : minus) {
                sum.r -= minusColor.r;
                sum.g -= minusColor.g;
                sum.b -= minusColor.b;
            }
            boolean satisfied =  ( sum.r == satisfaction.r
                    && sum.g == satisfaction.g
                    && sum.b == satisfaction.b );
            if(satisfied) {
                sockets[0] = fuseIndices.get(0);
                sockets[1] = fuseIndices.get(1);
                sockets[2] = fuseIndices.get(2);
            }
            return satisfied;
        }

        public String report() {
            return fuses[sockets[0]].name + ", " + fuses[sockets[1]].name + ", " + fuses[sockets[2]].name;
        }
    }

    // define the breakers and their requirements
    static final Breaker[] breakers = new Breaker[]{
            new Breaker(Color.WHITE, List.of(Color.CYAN)),
            new Breaker(Color.YELLOW, List.of(Color.BLUE,Color.BLUE)),
            new Breaker(Color.CYAN, List.of(Color.GREEN)),
            new Breaker(Color.PINK, List.of(Color.YELLOW)),
            new Breaker(Color.BLACK, List.of(Color.RED)),
            new Breaker(Color.WHITE, List.of(Color.GREEN, Color.RED)),
            new Breaker(Color.GREEN, List.of(Color.BLUE, Color.RED)),
            new Breaker(Color.BLUE, List.of()),
            new Breaker(Color.RED, List.of(Color.BLUE)),
            new Breaker(Color.BLACK, List.of(Color.WHITE, Color.RED)),
    };

    // track which fuses are in use, since we can only use each fuse once
    static final int [] fusesInUse = new int[fuses.length];

    public static void main(String[] args) {
        //Breaker test = new Breaker(Color.WHITE, List.of(Color.CYAN));
        //System.out.println("satisfied? " + (test.isSatisfied(List.of(18,5,17))?"Y":"N"));  // should be yes
        //System.out.println("satisfied? " + (test.isSatisfied(List.of(18,5,1))?"Y":"N"));  // should be no

        // iterate through all breakers and find a combination of fuses that satisfies all of them
        // this is a combinatorial problem, we can use backtracking to find a solution
        satisfyBreaker(0);
    }

    /**
     * Recursive function to satisfy each breaker in turn.
     * @param i the index of the breaker to satisfy.
     */
    private static void satisfyBreaker(int i) {
        System.out.println(i);
        if(i>= breakers.length) {
            // all satisfied
            printAnswerAndQuit();
        }

        Breaker breaker = breakers[i];
        // try all combinations of fuses for this breaker
        for (int j = 0; j < fuses.length; j++) {
            if (fusesInUse[j] != 0) continue;  // only use each fuse once
            fusesInUse[j]++;

            for (int k = j+1; k < fuses.length; k++) {
                if (fusesInUse[k] != 0) continue;  // only use each fuse once
                fusesInUse[k]++;

                for (int l = k+1; l < fuses.length; l++) {
                    if (fusesInUse[l] != 0) continue;  // only use each fuse once
                    fusesInUse[l]++;
                    if (breaker.isSatisfied(List.of(j, k, l))) {
                        satisfyBreaker(i + 1);
                    }
                    fusesInUse[l]--;
                }
                fusesInUse[k]--;
            }
            fusesInUse[j]--;
        }
    }

    private static void printAnswerAndQuit() {
        System.out.println("Found a solution!");
        for (int j = 0; j < breakers.length; j++) {
            System.out.println("Breaker " + j + " used " + breakers[j].report());
        }
        System.exit(0);
    }
}