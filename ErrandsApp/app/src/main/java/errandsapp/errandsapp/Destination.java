package errandsapp.errandsapp;

/**
 * Created by Joe on 10/8/2014.
 */
public class Destination {
    //public for testing purposes
    public String name;
    public double longitude;
    public double latitude;
    public String address;


    public Destination(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean equals(Destination otherDestination)
    {
        if(this.name.equals(otherDestination.name) && this.longitude == otherDestination.longitude && this.latitude == otherDestination.latitude) {
            return true;
        }
        return false;
    }
}
