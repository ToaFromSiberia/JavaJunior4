package org.junior;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junior.models.Person;
import org.junior.models.Phone;

import java.time.LocalDate;
import java.util.List;

public class Application {

    private SessionFactory factory;

    public Application() {
        setup();
    }

    public void run()
    {

        addPersonsToDB();


        showDb();

        System.out.println("Update person...");
        List<Person> persons = getAllPersons();
        Person person = getPersonById(persons.getFirst().getId());
        person.addPhone(new Phone("8-800-255-35-35", person));
        person = updatePerson(person);
        showDb();


        System.out.println("Delete person...");
        deletePerson(person);
        showDb();

        persons = getAllPersons();
        for (Person p : persons) {
            deletePerson(p);
        }

        close();
    }

    public List<Person> getAllPersons()
    {
        try (EntityManager manager = factory.createEntityManager())
        {
            return manager.createQuery("FROM Person", Person.class).getResultList();
        }
    }

    public Person getPersonById(long id)
    {
        try (EntityManager manager = factory.createEntityManager())
        {
            Query query = manager.createQuery("FROM Person WHERE id = :id", Person.class);
            query.setParameter("id", id);
            return  (Person) query.getSingleResult();
        }
    }


    public Person updatePerson(Person person)
    {
        if (person.getId() == 0L)
            return null;
        EntityTransaction transaction = null;
        try (EntityManager manager = factory.createEntityManager())
        {
            manager.detach(person);
            transaction = manager.getTransaction();
            transaction.begin();
            Person res = manager.merge(person);
            transaction.commit();
            return res;
        } catch (Exception e)
        {
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            throw e;
        }

    }

    public void addPerson(Person person, Phone ... phones)
    {
        EntityTransaction transaction = null;
        try (EntityManager manager = factory.createEntityManager())
        {
            transaction = manager.getTransaction();
            transaction.begin();
            for (Phone phone : phones)
            {
                person.addPhone(phone);
            }
            manager.persist(person);
            transaction.commit();
        } catch (Exception e)
        {
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            throw e;
        }
    }

    public void deletePerson(Person person)
    {
        if (person.getId() == 0L)
            return;
        EntityTransaction transaction = null;
        try (EntityManager manager = factory.createEntityManager())
        {
            transaction = manager.getTransaction();
            transaction.begin();
            manager.remove(person);
            transaction.commit();
        } catch (Exception e)
        {
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            throw e;
        }
    }

    private void addPersonsToDB()
    {
        addPerson(new Person("Lukin", "Igor", "Petrovich", LocalDate.of(1975, 8, 27)),
                new Phone("+7-918-240-27-11"),
                new Phone("+7-988-228-14-08")
        );
        addPerson(new Person("Nosonov", "Alexey", "Victorovich", LocalDate.of(1975, 9, 6)),
                new Phone("+7-906-283-62-11")
        );
    }

    private void setup()
    {
        final StandardServiceRegistry registry;

        registry = new StandardServiceRegistryBuilder().configure().build();
        try {
            factory = new MetadataSources(registry)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e)
        {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private void close()
    {
        if (factory != null)
            factory.close();
    }

    private void showDb()
    {
        System.out.println("Database: ");
        getAllPersons().forEach(System.out::println);
        System.out.println();
    }

}
