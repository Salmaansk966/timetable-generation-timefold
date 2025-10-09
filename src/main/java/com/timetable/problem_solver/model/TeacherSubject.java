package com.timetable.problem_solver.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class TeacherSubject {
    @ManyToOne
    @JoinColumn(name = "section_id_fk")
    private Section section;
    @ManyToOne
    @JoinColumn(name = "subject_id_fk")
    private Subject subject;

    @Override
    public boolean equals(Object other){
        if(other == null){
            return false;
        }
        if(this.getClass() != other.getClass()){
            return false;
        }
        return Objects.equals(this.section.getId(),((TeacherSubject) other).section.getId())
                && Objects.equals(this.subject.getId(), ((TeacherSubject) other).subject.getId());
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.section.getId(),this.subject.getId());
    }
}
