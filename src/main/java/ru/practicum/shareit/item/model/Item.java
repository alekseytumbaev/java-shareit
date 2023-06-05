package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private long id;
    
    @Column(name = "item_name")
    private String name;

    private String description;
    private Boolean available;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String request;
}
