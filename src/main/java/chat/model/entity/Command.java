package chat.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


/**
 * @author Alexander Diachenko.
 */
@Getter
@Setter
public class Command {

    private String name;
    private String description;
    private String response;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(this.name, command.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return "Command{" +
                "name='" + this.name + '\'' +
                ", description='" + this.description + '\'' +
                ", response='" + this.response + '\'' +
                '}';
    }
}
