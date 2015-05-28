

-------------------------------------------------------------------------
--Create trigger to update status from Processing to Completed 
--
-------------------------------------------------------------------------
create or replace TRIGGER update_status_trigger 
  after UPDATE of ticket_status ON tickets
  for each row
  when (new.ticket_status = 'Processing')
  BEGIN
  :new.ticket_status := 'Completed';
  END;


  -------------------------------------------------------------------------
--Create trigger to change status of termination request
--
-------------------------------------------------------------------------
create or replace TRIGGER update_termination_status_trigger 
  after UPDATE of status ON termination_requests
  for each row
  when (new.status = 'Processing')
  BEGIN
  :new.status := 'Completed';
  END;