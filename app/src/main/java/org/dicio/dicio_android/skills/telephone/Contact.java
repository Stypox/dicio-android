package org.dicio.dicio_android.skills.telephone;

import java.util.Comparator;

public class Contact implements Comparable {

    String name;
    int distance;
    String id;
    public Contact(String name, int distance,String id){
        this.name=name;
        this.distance=distance;
        this.id=id;

    }



    @Override
    public int compareTo(Object o) {
        return distance-((Contact)o).distance;
    }
}
