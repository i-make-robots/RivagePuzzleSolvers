package com.rivage;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Solve a gear problem in the video game Rivage.</p>
 * <p>We are given:</p>
 * <ul>
 * <li>8 wheels, each with a unique combination of six possible pins.</li>
 * <li>8 spokes on which wheels can be placed.</li>
 * <li>two power input pins</li>
 * <li>three pairs of symbolic output pins (Square, Triangle, Circle).</li>
 * <li>a lever.  when pulled, it rotates all the wheels 1/6 of a turn 6 times.  At each step, it checks the output pins to see if they are satisfied.</li>
 * </ul>
 * <p>The goal is to find a combination of wheels that satisfies the correct output pins at each of the six steps.</p>
 * <p>When two adjacent spokes have wheels and the wheel pins touch, a connection
 * is made. When there is a series of connections from the input pins to the output pins, the output pins are activated.
 * The condition for the output pins to be satisfied is that the correct combination of input pins must be connected to
 * the correct combination of output pins.</p>
 */
public class RivageSolver2 {
    /**
     * Wheel has six pins, each of which can be either present or absent.
     * The pins are represented as bits in a char.  A 1 indicates there is a pin.
     */
    static class Wheel {
        char pins;  // initial pin positions
        final String name;

        // input 1s and 0s to represent the presence or absence of a pin on the wheel.
        public Wheel(char pins, String name) {
            this.pins = pins;
            this.name = name;
        }

        /**
         * bit-shift and rotate the 6 pins.
         * @param times the number of times to rotate the wheel.  Each rotation is 1/6 of a turn.
         * @return the new pin configuration after rotating the wheel the specified number of times.
         */
        public char turn(int times) {
            int shift = (5-times) % 6;
            return (char)(((pins << shift) | (pins >> (6 - shift))) & 0b111111);
        }

        public String toString() {
            return name;
        }
    }

    // wheels that can be moved.
    static final Wheel [] wheels = new Wheel[] {
            new Wheel( (char)0b101100,"A"),  // can be moved
            new Wheel( (char)0b110010,"B"),  // can be moved
            new Wheel( (char)0b101110,"C"),  // can be moved
            new Wheel( (char)0b101110,"D"),  // can be moved
            new Wheel( (char)0b001001,"E"),  // can be moved
            new Wheel( (char)0b011010,"F"),  // can be moved
            new Wheel( (char)0b110110,"LockN"),
            new Wheel( (char)0b010101,"LockS"),
            // unmovable pins that provide power
            new Wheel( (char)0b111111,"Input1"),
            new Wheel( (char)0b111111,"Input2"),
            // unmovable output pins that receive power
            new Wheel( (char)0b111111,"OutputSquare"),
            new Wheel( (char)0b111111,"OutputTriangle"),
            new Wheel( (char)0b111111,"OutputCircle")
    };


    /**
     * 6 spokes where the wheels can be changed, three that are locked, two that are inputs, and one of each output.
     */
    static final int SPOKE_A = 0;
    static final int SPOKE_B = 1;
    static final int SPOKE_C = 2;
    static final int SPOKE_D = 3;
    static final int SPOKE_E = 4;
    static final int SPOKE_F = 5;
    static final int NUM_UNLOCKED_SPOKES = 6;
    static final int SPOKE_LOCKN = 6;
    static final int SPOKE_LOCKS = 7;
    static final int SPOKE_INPUT1 = 8;
    static final int SPOKE_INPUT2 = 9;
    static final int SPOKE_SQUARE = 10;
    static final int SPOKE_CIRCLE = 11;
    static final int SPOKE_TRIANGLE = 12;
    static final int NUM_SPOKES = 13;

    /**
     * A map of the possible connections between spokes.  Each spoke has six pins, numbered 1-6.
     * A connection is made when two adjacent spokes have wheels and the pins touch.
     */
    static class FlowNode {
        int spoke1, pin1;
        int spoke2, pin2;

        public FlowNode(int spoke1, int pin1, int spoke2, int pin2) {
            this.spoke1 = spoke1;
            this.pin1 = pin1;
            this.spoke2 = spoke2;
            this.pin2 = pin2;
        }
    }

    /**
     * The flow map is arranged as follows:
     * <pre>
     *      i1        i2
     *         lockn
     *        A     B
     *      C  locks  D
     * Square E     F  circle
     *        triangle
     * </pre>
     * spokes are
     * <pre>
     *     5 0
     *    4   1
     *     3 2
     * </pre>
     * So a i1.2 touches lockn.5 and i2.3 touches lockn.0
     * Power can flow either way through a connection.
     */
    static final List<FlowNode> flowMap = List.of(
            new FlowNode(SPOKE_INPUT1,2,SPOKE_LOCKN ,5),
            new FlowNode(SPOKE_INPUT2,3,SPOKE_LOCKN ,0),

            new FlowNode(SPOKE_LOCKN ,2,SPOKE_B     ,5),
            new FlowNode(SPOKE_LOCKN ,3,SPOKE_A     ,0),

            new FlowNode(SPOKE_A     ,1,SPOKE_B     ,4),

            new FlowNode(SPOKE_A     ,3,SPOKE_C     ,0),
            new FlowNode(SPOKE_A     ,2,SPOKE_LOCKS ,5),
            new FlowNode(SPOKE_B     ,3,SPOKE_LOCKS ,0),
            new FlowNode(SPOKE_B     ,2,SPOKE_D     ,5),

            new FlowNode(SPOKE_C     ,1,SPOKE_LOCKS ,4),
            new FlowNode(SPOKE_LOCKS ,1,SPOKE_D     ,4),

            new FlowNode(SPOKE_C     ,3,SPOKE_SQUARE,0),
            new FlowNode(SPOKE_C     ,2,SPOKE_E     ,5),
            new FlowNode(SPOKE_LOCKS ,3,SPOKE_E     ,0),
            new FlowNode(SPOKE_LOCKS ,2,SPOKE_F     ,5),
            new FlowNode(SPOKE_D     ,3,SPOKE_F     ,0),
            new FlowNode(SPOKE_D     ,2,SPOKE_CIRCLE,5),

            new FlowNode(SPOKE_SQUARE,1,SPOKE_E     ,4),
            new FlowNode(SPOKE_E     ,1,SPOKE_F     ,4),
            new FlowNode(SPOKE_F     ,1,SPOKE_CIRCLE,4),

            new FlowNode(SPOKE_E,2,SPOKE_TRIANGLE   ,5),
            new FlowNode(SPOKE_F,3,SPOKE_TRIANGLE   ,0)
    );

    /**
     * 3 bits to represent the presence or absence of each of the three output pins.  The bits are in the order: Square, Triangle, Circle.
     */
    static final List<Integer> conditions = List.of(
            0b111000,  // step 0
            0b011000,  // step 1
            0b110000,  // step 2
            0b100000,  // step 3
            0b111000,  // step 4
            0b011000   // step 5
    );
    static final char OUTPUT_SQUARE   = 5;
    static final char OUTPUT_TRIANGLE = 4;
    static final char OUTPUT_CIRCLE   = 3;


    static class SpokeSet {
        final int [] myIndexes = new int[NUM_SPOKES];  // index of the wheels placed on the first six spokes.
        boolean [] isPowered = new boolean[NUM_SPOKES];  // whether the spoke is powered or not.

        public SpokeSet(int [] wheelIndexes) {
            myIndexes[0] = wheelIndexes[0];
            myIndexes[1] = wheelIndexes[1];
            myIndexes[2] = wheelIndexes[2];
            myIndexes[3] = wheelIndexes[3];
            myIndexes[4] = wheelIndexes[4];
            myIndexes[5] = wheelIndexes[5];
            for(int i=6;i<NUM_SPOKES;i++) {
                myIndexes[i] = i;
            }
        }

        public String toString() {
            return ""
                    + wheels[myIndexes[0]].name
                    + wheels[myIndexes[1]].name
                    + wheels[myIndexes[2]].name
                    + wheels[myIndexes[3]].name
                    + wheels[myIndexes[4]].name
                    + wheels[myIndexes[5]].name;
        }

        private void flow(int steps) {
            Arrays.fill(isPowered, false);
            isPowered[SPOKE_INPUT1] = true;
            isPowered[SPOKE_INPUT2] = true;

//            System.out.println("Step " + steps + ": " + this);
//            int count=0;
            boolean [] changed=new boolean[]{false};
            do {
//                System.out.println("isPowered: " + Arrays.toString(isPowered));
                changed[0] = false;
//                count++;
                flowMap.forEach(flow -> {
                    // if either spoke is powered, power could flow to the other spoke.
                    int s1 = myIndexes[flow.spoke1];
                    int s2 = myIndexes[flow.spoke2];
                    boolean spoke1Powered = isPowered[s1];
                    boolean spoke2Powered = isPowered[s2];
                    if (spoke1Powered != spoke2Powered) {
                        char w1pins = wheels[s1].turn(steps);
                        char w2pins = wheels[s2].turn(steps);
//                        System.out.println(
//                                s1 + ":" + flow.pin1 + "@" + wheelPinAsString(w1pins) + " " + (spoke1Powered?"P":"U") + (wheelPin(w1pins,flow.pin1)?"T":"F")+ " <-> " +
//                                s2 + ":" + flow.pin2 + "@" + wheelPinAsString(w2pins) + " " + (spoke2Powered?"P":"U") + (wheelPin(w2pins,flow.pin2)?"T":"F") );
                        // check if the pins are touching.  If they are, power can flow to the other spoke.
                        if(wheelPin(w1pins,flow.pin1) && wheelPin(w2pins,flow.pin2)) {
                            // connection made, power can flow to the other spoke.
//                            System.out.println("touching");
                            isPowered[s1] = true;
                            isPowered[s2] = true;
                            changed[0] = true;
                        }
                    }
                });
            } while( changed[0] );
//            System.out.println("flows in " + count + " steps.");
        }

        public String flowState(int steps) {
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<6;++i) {
                sb.append(wheelPinAsString(wheels[myIndexes[i]].turn(steps)));
                sb.append(" ");
            }
            return sb.toString();
        }
    }

    /**
     * Main entry point
     * @param args
     */
    public static void main(String[] args) {
        //testRotation();
        //testWheelPins();

        // test the initial flow of power from the input spokes to the output spokes.
        System.out.println("satisfied1:"+countSatisfiedConditions(new SpokeSet(new int[]{0,1,2,3,4,5})));
        System.out.println("satisfied2:"+countSatisfiedConditions(new SpokeSet(new int[]{0,1,2,3,5,4})));

        // iterate over every combination of wheels and spokes, and check if the output pins are satisfied at each step.
        checkAllPossibleSolutions();
    }


    static boolean wheelPin(char wheelPins, int pinNumber) {
        return (wheelPins & (1<< (5-pinNumber))) != 0;
    }

    static void checkAllPossibleSolutions() {
        int i=0;

        for(int a=0;a<6;a++) {
            for(int b=0;b<6;b++) {
                if(a==b) continue;

                for(int c=0;c<6;c++) {
                    if(a==c||b==c) continue;

                    for(int d=0;d<6;d++) {
                        if(a==d||b==d||c==d) continue;

                        for(int e=0;e<6;e++) {
                            if(a==e||b==e||c==e||d==e) continue;

                            for(int f=0;f<6;f++) {
                                if(a==f||b==f||c==f||d==f||e==f) continue;

                                SpokeSet spokes = new SpokeSet(new int[]{a,b,c,d,e,f});
                                System.out.print(i+": "+spokes+" = ");
                                if( countSatisfiedConditions(spokes) == 18 ) System.exit(0);
                                i++;
                            }
                        }
                    }
                }
            }
        }
    }

    private static int countSatisfiedConditions(SpokeSet set) {
        int passed=0;
        int i=0;
        StringBuilder sb = new StringBuilder();
        for(Integer condition : conditions) {
            set.flow(i);
            //sb.append("\n  ").append(set.flowState(i)).append(" ");
            if(wheelPin((char) condition.byteValue(), OUTPUT_SQUARE) == set.isPowered[SPOKE_SQUARE]) {
                sb.append("Y");
                passed++;
            } else {
                sb.append("N");
            }
            if(wheelPin((char)condition.byteValue(),OUTPUT_CIRCLE  ) == set.isPowered[SPOKE_CIRCLE  ]) {
                sb.append("Y");
                passed++;
            } else {
                sb.append("N");
            }
            if(wheelPin((char)condition.byteValue(),OUTPUT_TRIANGLE) == set.isPowered[SPOKE_TRIANGLE]) {
                sb.append("Y");
                passed++;
            } else {
                sb.append("N");
            }
            sb.append(" ");
            i++;
        }
        sb.append(passed);
        System.out.println(sb);

        return passed;
    }

    // should produce the same output as testRotation()
    private static void testWheelPins() {
        for(int i=0;i<6;i++) {
            System.out.println( wheelPinAsString(wheels[0].turn(i)) );
        }
    }

    private static String wheelPinAsString(char pins) {
        return (wheelPin(pins,0)?"1":"0") +
               (wheelPin(pins,1)?"1":"0") +
               (wheelPin(pins,2)?"1":"0") +
               (wheelPin(pins,3)?"1":"0") +
               (wheelPin(pins,4)?"1":"0") +
               (wheelPin(pins,5)?"1":"0");
    }

    // confirm that rotating pins with bit shifting produces the right result.
    private static void testRotation() {
        Wheel w = wheels[0];
        for(int i=0;i<6;i++) {
            System.out.println( Integer.toBinaryString(w.turn(i)) );
        }
    }
}
