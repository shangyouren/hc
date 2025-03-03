package hc.directory.local.disk.pojo;

import lombok.Data;

import java.util.List;

@Data
public class Paged<T> {

    private List<T> data;

    private String lastName;

    public Paged(List<T> list, String newLastName) {
        this.data= list;
        this.lastName = newLastName;
    }
}
