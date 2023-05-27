package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_generator")
    @SequenceGenerator(name = "item_generator", sequenceName = "item_seq")
    @Column(name = "item_id")
    private long id;
    
    @Column(name = "item_name")
    private String name;

    private String description;
    private Boolean available;

    @ManyToOne(optional = false)
    private User owner;

    private String request;
}
