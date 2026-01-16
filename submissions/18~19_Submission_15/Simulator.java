import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing rabbits and foxes.
 *
 * @version 2019.02.22 (3)
 */
public class Simulator
{
    private static final Random rand = Randomizer.getRandom();
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 150;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 150;
    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.022;
    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.08;
    // The probability that a eagle will be created in any given grid position.
    private static final double EAGLE_CREATION_PROBABILITY = 0.0435;
    // The probability that a Blue jay will be created in any given grid position.
    private static final double BLUEJAY_CREATION_PROBABILITY = 0.05;
    // The probability that a worm will be created in any given grid position.
    private static final double WORM_CREATION_PROBABILITY = 0.023;
    // The probability that a berry will be created in any given grid position.
    private static final double BERRY_CREATION_PROBABILITY = 0.035;

    // List of animals in the field.
    private List<Animal> animals;
    // List of plant in the field.
    private List<Plant> plants;
    //Array List of all possible weather conditions in the simulation.
    private ArrayList<String> weatherPossibilities = new ArrayList<>();
    {{
        weatherPossibilities.add("rain");
        weatherPossibilities.add("clear");
    }}

    private static String currentWeather = "clear";
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    //The time represented in the system.
    private static int currentTime = 0;

    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }

        plants = new ArrayList<>();
        animals = new ArrayList<>();
        field = new Field(depth, width);

        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Rabbit.class, Color.ORANGE);
        view.setColor(Fox.class, Color.RED);
        view.setColor(Eagle.class, Color.YELLOW);
        view.setColor(Worm.class, Color.MAGENTA);
        view.setColor(Bluejay.class, Color.BLUE);
        view.setColor(Berry.class, Color.GREEN);
        
        // Setup a valid starting point.
        reset();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long period,
     * (4200 steps).
     */
    public void runLongSimulation()
    {
        simulate(4200);
    }
    
    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        for(int step = 1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
            // delay(60);   // uncomment this to run more slowly
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * fox and rabbit.
     */
    public void simulateOneStep()
    {
        step++;
        calculateTime();


        // Provide space for newborn animals.
        List<Animal> newAnimals = new ArrayList<>();
        List<Plant> newPlants = new ArrayList<>();
        // Let all rabbits act.
        for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            animal.act(newAnimals);
            if(! animal.isAlive()) {
                it.remove();
            }
        }
        for(Iterator<Plant> it = plants.iterator(); it.hasNext(); ) {
            Plant plant = it.next();
            plant.grow(newPlants);
            if(!plant.isAlive()) {
                it.remove();
            }
        }
               
        // Add the newly born foxes and rabbits to the main lists.
        animals.addAll(newAnimals);

        plants.addAll(newPlants);

        view.showStatus(step, field, currentTime);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        currentTime = 0;
        animals.clear();
        plants.clear();
        populate();
        
        // Show the starting state in the view.
        view.showStatus(step, field, currentTime);
    }
    
    /**
     * Randomly populate the field with animals and plants.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Fox fox = new Fox(true, field, location, genderGenerator());
                    animals.add(fox);
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Rabbit rabbit = new Rabbit(true, field, location, genderGenerator());
                    animals.add(rabbit);
                }
                else if(rand.nextDouble()<=EAGLE_CREATION_PROBABILITY){
                    Location location = new Location(row, col);
                    Eagle eagle = new Eagle(true, field, location, genderGenerator());
                    animals.add(eagle); 
                }
                else if(rand.nextDouble()<=BLUEJAY_CREATION_PROBABILITY){
                    Location location = new Location(row, col);
                    Bluejay bluejay = new Bluejay(true, field, location, genderGenerator());
                    animals.add(bluejay);
                }
                else if(rand.nextDouble()<=WORM_CREATION_PROBABILITY){
                    Location location = new Location(row, col);
                    Worm worm = new Worm(true, field, location, genderGenerator());
                    animals.add(worm);
                }
                else if(rand.nextDouble()<=BERRY_CREATION_PROBABILITY){
                    Location location = new Location(row, col);
                    Berry berry = new Berry(true, field, location);
                    plants.add(berry);
                }
                // else leave the location empty.
            }
        }
    }
    
    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    private void delay(int millisec)
    {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            /* wake up */
        }
    }

    /**
     * Calculates time given the step counnt and changes weather every four hours
     * @return currentTime
     */
    private int calculateTime(){
        int hourMeasure = step%25;
        if (hourMeasure == 0){
            currentTime++;
            currentTime = currentTime%24;
            changeWeather();
        }
        return currentTime;
    }

    /**
     * Generates a random gender for each annimal.
     * @return true if gender is male
     */
    private boolean genderGenerator(){
    int genderDeterminer = rand.nextInt(2);
    if (genderDeterminer == 0)
        return true;
    
    return false;
    }

    /**
     * Checks if the current time falls in the day
     * @return true if is day
     */

    public static boolean isDay(){
        return (currentTime <20 && currentTime >= 8) ;
    }
    /**
     * Checks if the current time falls in the evening
     * @return true if is evening
     */

    public static boolean isEvening(){
        return (currentTime > 18 && currentTime <= 20);
    }
    /**
     * Checks if the current time falls in the morning
     * @return true if is morning
     */
    public static boolean isMorning() {
        return (currentTime >= 6 && currentTime < 8);
    }

    /**
     * returns current weather state
     * @return currentWeather
     */
    public static String getCurrentWeather(){
        return currentWeather;
    }

    /**
     * Generates a random weather condition from a given Array List
     * @return currentWeather
     */

    private String setRandomWeather(){
        currentWeather = weatherPossibilities.get(rand.nextInt(weatherPossibilities.size()));
        return currentWeather;
    }
    /**
     * Generates a random weather condition from a given Array List
     * and sets it to the current weather state every four hours
     * @return currentWeather
     */

    private String changeWeather(){
        if ((currentTime%6) == 0){
            setRandomWeather();
        }
        return currentWeather;
    }
}

