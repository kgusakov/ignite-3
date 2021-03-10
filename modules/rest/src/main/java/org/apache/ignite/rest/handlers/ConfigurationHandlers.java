package org.apache.ignite.rest.handlers;

import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import org.apache.ignite.configuration.internal.selector.SelectorNotFoundException;
import org.apache.ignite.configuration.validation.ConfigurationValidationException;
import org.apache.ignite.rest.ErrorResult;
import org.apache.ignite.rest.presentation.ConfigurationPresentation;

public class ConfigurationHandlers {
    private volatile ConfigurationPresentation<String> presentation;

    public ConfigurationHandlers(ConfigurationPresentation<String> presentation) {
       this.presentation = presentation;
    }

   public void get(Context ctx) {
      ctx.result(presentation.represent());
   }

   public void getByPath(Context ctx) {
      String configPath = ctx.pathParam("selector");

      try {
         ctx.result(presentation.representByPath(configPath));
      }
      catch (SelectorNotFoundException | IllegalArgumentException pathE) {
         ErrorResult eRes = new ErrorResult("CONFIG_PATH_UNRECOGNIZED", pathE.getMessage());

         ctx.status(400).json(eRes);
      }
   }

   public void set(Context ctx) {
      try {
         presentation.update(ctx.body());
      }
      catch (SelectorNotFoundException | IllegalArgumentException argE) {
         ErrorResult eRes = new ErrorResult("CONFIG_PATH_UNRECOGNIZED", argE.getMessage());

         ctx.status(400).json(eRes);
      }
      catch (ConfigurationValidationException validationE) {
         ErrorResult eRes = new ErrorResult("APPLICATION_EXCEPTION", validationE.getMessage());

         ctx.status(400).json(eRes);
      }
      catch (JsonSyntaxException e) {
         String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();

         ErrorResult eRes = new ErrorResult("VALIDATION_EXCEPTION", msg);

         ctx.status(400).json(eRes);
      }
      catch (Exception e) {
         ErrorResult eRes = new ErrorResult("VALIDATION_EXCEPTION", e.getMessage());

         ctx.status(400).json(eRes);
      }
   }
}
