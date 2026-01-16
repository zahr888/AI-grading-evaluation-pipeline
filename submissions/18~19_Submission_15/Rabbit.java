import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a rabbit.
 * Rabbits age, move, eat berries, and die.
 *
 * @version 2019.02.22 (3)
 */
public class Rabbit extends Animal {
    // Characteristics shared by all rabbits (class variables).

    // The age at which a rabbit can start to breed.
    private static final int BREEDING_AGE = 5;
    // The age to which a rabbit can live.
    private static final int MAX_AGE = 50;
    // The likelihood of a rabbit breeding.
    private static final double BREEDING_PROBABILITY = 0.28;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 5;
    // A shared random number generator to control breeding.
    private static final int BERRY_FOOD_VALUE = 9;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //The amount of accumulated toxin the rabbit can ingest before dying
    private static final int toxinImmunity = 60;

    // Individual characteristics (instance fields).

    // The rabbit's age and food level.
    private int age;
    private int foodLevel;

    /**
     * Create a new rabbit. A rabbit may be created with age
     * zero (a new born) or with a random age.
     *
     * @param randomAge If true, the rabbit will have a random age.
     * @param field     The field currently occupied.
     * @param location  The location within the field.
     * @param  gender The gender of the rabbit
     */
    public Rabbit(boolean randomAge, Field field, Location location, boolean gender) {
        super(field, location, gender);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(BERRY_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = BERRY_FOOD_VALUE;
        }
    }


    /**
     * This is what the rabbit does most of the time: it searches for
     * berries. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param newRabbits A list to return newly born rabits.
     */
    public void act(List<Animal> newRabbits) {
        if (Simulator.isDay()) {
            incrementAge();
            incrementHunger();
            if (isAlive()) {
                giveBirth(newRabbits);
                // Try to move into a free location.
                Location newLocation = findFood();
                if (newLocation == null) {
                    // No food found - try to move to a free location.
                    newLocation = getField().freeAdjacentLocation(getLocation());
                }
                // See if it was possible to move.
                if (newLocation != null) {
                    setLocation(newLocation);
                    if (checkToxin(toxinImmunity)) {
                        setDead();
                    }
                } else {
                    // Overcrowding.
                    setDead();
                }
            }
        }
    }

    /**
     * Increase the age.
     * This could result in the rabbit's death.
     */
    private void incrementAge() {
        age++;
        if (age > MAX_AGE) {
            setDead();
        }
    }
    /**
     * Make the Rabbit hungrier.
     * This could result in the rabbit's death.
     */

    private void incrementHunger() {
        foodLevel--;
        if (foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Check whether or not this rabbit is to give birth at this step.
     * New births will be made into free adjacent locations.
     *
     * @param newRabbits A list to return newly born rabbits.
     */
    private void giveBirth(List<Animal> newRabbits) {
        // New rabbits are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for (int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Rabbit young = new Rabbit(false, field, loc, generateRandomGender());
            newRabbits.add(young);
        }
    }
    /**
     * Look for berries adjacent to the current location.
     * Only the first berry is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood() {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while (it.hasNext()) {
            Location where = it.next();
            Object plant = field.getObjectAt(where);
            Object animal = field.getObjectAt(where);
            if (plant instanceof Berry) {
                Berry berry = (Berry) plant;
                if (berry.isAlive()) {
                    currentToxinLevel = currentToxinLevel + berry.getToxinLevel();
                    berry.setDead();
                    foodLevel = BERRY_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }

    /**
     * Generate a number representing the number of births,
     * if it can breed.
     *
     * @return The number of births (may be zero).
     */
    private int breed() {
        int births = 0;
        if (canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A rabbit can breed if it has reached the breeding age.
     *and if the adjacent rabbit is of the opposite sex
     * @return true if the rabbit can breed, false otherwise.
     */
    private boolean canBreed() {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while (it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if (animal instanceof Rabbit && ((Rabbit) animal).getGender() != getGender() && age >= BREEDING_AGE)
                return true;
        }
        return false;
    }
}


