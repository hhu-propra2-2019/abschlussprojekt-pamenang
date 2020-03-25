package mops.klausurzulassung.services;

import mops.klausurzulassung.domain.EmailError;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class EmailErrorService {

    ArrayList<EmailError> emailErrors = new ArrayList<>();

    public void addEmailErrorToList(EmailError emailError){
      this.emailErrors.add(emailError);
    }

    public ArrayList<EmailError> getEmailErrors(){
      return this.emailErrors;
    }

    public void deleteEmailErrorFromList(EmailError emailError){
      emailErrors.remove(emailError);
    }
}
