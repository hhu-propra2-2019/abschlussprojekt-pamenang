package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrontendMessage {

  private String errorMessage;
  private String successMessage;

  public void resetMessage(){
    this.setErrorMessage(null);
    this.setSuccessMessage(null);
  }


}
