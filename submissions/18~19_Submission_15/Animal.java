import java.util.List;
import java.util.Random;

/**
 * A class representing shared characteristics of animals.
 * @version 2016.02.29 (2)
 */
public abstract class Animal
{
    // Whether the animal is alive or not.
    private boolean alive;
    // The animal's field.
    private Field field;
    // The animal's position in the field.
    private Location location;
    // Whether an animal is male or female;
    private boolean gender;
    // The Toxin content in a animal
    protected int currentToxinLevel;
    //The random gender generated for animals
    private boolean randomGender;
    //random object used to control gender assignments
    private static final Random rand = Randomizer.getRandom();
    /**
     * Create a new animal at location in field.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param gender The gender of each animal.
     */
    public Animal(Field field, Location location, boolean gender)
    {
        alive = true;
        this.field = field;
        this.gender = gender;
        setLocation(location);
        currentToxinLevel = 0;
    }
    
    /**
     * Make this animal act - that is: make it do
     * whatever it wants/needs to do.
     * @param newAnimals A list to receive newly born animals.
     */
    abstract public void act(List<Animal> newAnimals);

    /**
     * Check whether the animal is alive or not.
     * @return true if the animal is still alive.
     */
    protected boolean isAlive()
    {
        return alive;
    }

    /**
     * Indicate that the animal is no longer alive.
     * It is removed from the field.
     */
    protected void setDead()
    {
        alive = false;
        if(location != null) {
            field.clear(location);
            location = null;
            field = null;
        }
    }

    /**
     * Return the animal's location.
     * @return The animal's location.
     */
    protected Location getLocation()
    {
        return location;
    }
    
    /**
     * Place the animal at the new location in the given field.
     * @param newLocation The animal's new location.
     */
    protected void setLocation(Location newLocation)
    {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }
    
    /**
     * Return the animal's field.
     * @return The animal's field.
     */
    protected Field getField(){
        return field;
    }

    /**
     * Returns an animals gender
     * @return gender
     */
    protected  boolean getGender(){
        return this.gender;
    }

    /**
     * Generates Random gender
     * @return randomized gender
     */
    protected boolean generateRandomGender(){
        int genderDeterminer = rand.nextInt(2);
        if (genderDeterminer == 0){
            randomGender = true;
        }
        else{
            randomGender = false;
        }
        return randomGender;
    }

    /**
     * Checks if the animals toxin content is greater
     * than its immunity
     * @param toxinImmunity
     * @return true if toxin level exceeds immunity
     */

    protected boolean checkToxin(int toxinImmunity){
       return (currentToxinLevel > toxinImmunity);
    }

    /**
     * Returns current toxin Level of an animal
     * @return current toxin level
     */

    protected int getToxinLevel(){
        return currentToxinLevel;
    }


}