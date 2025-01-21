package firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class FirebaseRecoverObject {

    public static void main(String[] args) throws FileNotFoundException {
        Item item = new Item();
        item.setId(180L);
        item.setName("PruebaNetbeans");
        item.setPrice(180.0);

        // save item objec to firebase.
        //new FirebaseSaveObject().save(item);
        new FirebaseRecoverObject().recover();
    }

    private FirebaseDatabase firebaseDatabase;

    /**
         * inicialización de firebase.
*/
    private void initFirebase() {
        
        try {
            FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()   
                    .setDatabaseUrl("https://poo-proyecto-2b692-default-rtdb.firebaseio.com/")

                    .setServiceAccount(new FileInputStream(new File("C:\\Users\\Bryan\\Documents\\NetBeansProjects\\Firebase-master\\poo-proyecto-2b692-firebase-adminsdk-fbsvc-1b3ef97d2f.json")))

                    .build();

            FirebaseApp.initializeApp(firebaseOptions);
            firebaseDatabase = FirebaseDatabase.getInstance();
            System.out.println("Conexión exitosa....");
        }catch (RuntimeException ex) {
            System.out.println("Problema ejecucion ");
        }catch (FileNotFoundException ex) {
            System.out.println("Problema archivo");
        }

         
    }

    /**
     * Save item object in Firebase.
     * @param item 
     */
    private void save(Item item) throws FileNotFoundException {
        if (item != null) {
            initFirebase();
            
            /* Get database root reference */
            DatabaseReference databaseReference = firebaseDatabase.getReference("/");
            
            /* Get existing child or will be created new child. */
            DatabaseReference childReference = databaseReference.child("item");

            /**
             * The Firebase Java client uses daemon threads, meaning it will not prevent a process from exiting.
             * So we'll wait(countDownLatch.await()) until firebase saves record. Then decrement `countDownLatch` value
             * using `countDownLatch.countDown()` and application will continues its execution.
             */
            CountDownLatch countDownLatch = new CountDownLatch(1);
            childReference.setValue(item, new DatabaseReference.CompletionListener() {

                @Override
                public void onComplete(DatabaseError de, DatabaseReference dr) {
                    System.out.println("Registro guardado!");
                    // decrement countDownLatch value and application will be continues its execution.
                    countDownLatch.countDown();
                }
            });
            try {
                //wait for firebase to saves record.
                countDownLatch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void recover() {
    initFirebase();

    /* Obtén la referencia a la raíz de la base de datos */
    DatabaseReference databaseReference = firebaseDatabase.getReference("item");

    /* Escucha cambios en la referencia */
    CountDownLatch countDownLatch = new CountDownLatch(1);
    databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                // Convierte los datos recuperados en un objeto Item
                Item item = dataSnapshot.getValue(Item.class);

                if (item != null) {
                    System.out.println("Objeto recuperado:");
                    System.out.println("ID: " + item.getId());
                    System.out.println("Name: " + item.getName());
                    System.out.println("Price: " + item.getPrice());
                } else {
                    System.out.println("No se pudo mapear el objeto Item.");
                }
            } else {
                System.out.println("No hay datos disponibles en la referencia 'item'.");
            }

            countDownLatch.countDown();
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.err.println("Error al leer los datos: " + error.getMessage());
            countDownLatch.countDown();
        }
    });

    try {
        // Espera a que se completen las operaciones de Firebase
        countDownLatch.await();
    } catch (InterruptedException ex) {
        ex.printStackTrace();
    }
}
}
//Realizar la recuperacion de informacion
//perfeccionar el metodo en que se realiza una operacion, suprimir el conteo regresivo