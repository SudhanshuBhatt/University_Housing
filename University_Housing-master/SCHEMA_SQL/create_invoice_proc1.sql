set feedback on;
set serveroutput on;


CREATE OR REPLACE procedure create_invoice(v_person_id IN NUMBER,
                      v_accomodation_id IN NUMBER,
                      v_date_increment IN NUMBER,
                      v_mode_of_payment IN person_acc_staff.mode_of_payment%TYPE,
                      v_accomodation_type IN VARCHAR2,
                      v_input_count IN NUMBER,
                      v_output OUT NUMBER)
AS

v_living_rent           invoice_person_lease.MONTHLY_HOUSING_RENT%TYPE;
v_parking_fees           invoice_person_lease.MONTHLY_PARKING_RENT%TYPE :=100;
v_late_fees           NUMBER :=50;
v_incidentalCharges        NUMBER:=20;

v_max_invoice_no        invoice_person_lease.invoice_no%TYPE:=0;
v_move_in_date          DATE;
v_payment_date          DATE;
v_lease_no            person_accomodation_lease.lease_no%TYPE;
--v_mode_of_payment        person_acc_staff.mode_of_payment%TYPE;
v_payment_status        VARCHAR2(40) :='Outstanding';
v_total_payment_due        invoice_person_lease.payment_due%TYPE;
v_damage_charges        NUMBER:=0;
v_count                 NUMBER ;

err_code                  VARCHAR2(500);
err_msg                   VARCHAR2(500);

BEGIN
  
    v_output :=0;                     
   

   IF (v_accomodation_type = 'Family Apartment') THEN

       BEGIN

      select fa.monthly_rent, pal.lease_move_in_date,pal.lease_no
      into v_living_rent,v_move_in_date,v_lease_no
      from family_apartment fa,person_accomodation_lease pal
      where fa.accomodation_id = pal.accomodation_id
      and pal.accomodation_id = v_accomodation_id
      and pal.person_id = v_person_id;

    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        v_output:=-1;
        err_code := SQLCODE;
        err_msg := SUBSTR(SQLERRM, 1, 200)||'family apartment::v_accomodation_id:'||v_accomodation_id||'v_person_id:'||v_person_id;
        dbms_output.put_line('error_code: '||err_code||' error_message: '||err_msg);
        insert into error_table values('create_invoice_procedure',err_code,err_msg,sysdate);
        commit;
        return;

    END;
   
   ELSIF ((v_output = 0) AND (v_accomodation_type = 'Residence Hall')) THEN

       BEGIN

      select ra.monthly_rent_rate, pal.lease_move_in_date,pal.lease_no
      into v_living_rent,v_move_in_date,v_lease_no
      from residence_hall_provides_room ra,person_accomodation_lease pal
      where ra.accomodation_id = pal.accomodation_id
      and pal.accomodation_id = v_accomodation_id
      and pal.person_id = v_person_id;

    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        v_output:=-1;
       err_code := SQLCODE;
        err_msg := SUBSTR(SQLERRM, 1, 200)||'Residence Hall::v_accomodation_id:'||v_accomodation_id||'v_person_id:'||v_person_id;
        dbms_output.put_line('error_code: '||err_code||' error_message: '||err_msg);
        insert into error_table values('create_invoice_procedure',err_code,err_msg,sysdate);
        commit;
        dbms_output.put_line('BEFORE RETURN in RESIDENCE HALL');
        return;
        dbms_output.put_line('AFTER RETURN in RESIDENCE HALL');

    END;

   ELSIF ((v_output = 0) AND (v_accomodation_type = 'Bedroom') OR (v_accomodation_type = 'Apartment' )) THEN

       BEGIN

      select bd.monthly_rent_rate, pal.lease_move_in_date,pal.lease_no
      into v_living_rent,v_move_in_date,v_lease_no
      from bedroom bd,person_accomodation_lease pal
      where bd.accomodation_id = pal.accomodation_id
      and pal.accomodation_id = v_accomodation_id
      and pal.person_id = v_person_id;

    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        v_output:=-1;
        err_code := SQLCODE;
        err_msg := SUBSTR(SQLERRM, 1, 200)||'Bedroom or Apartment::v_accomodation_id:'||v_accomodation_id||'v_person_id:'||v_person_id;
        dbms_output.put_line('error_code: '||err_code||' error_message: '||err_msg);
        insert into error_table values('create_invoice_procedure',err_code,err_msg,sysdate);
        commit;
        return;

    END;

  END IF; --END OF IF CONDITION

IF (v_output = -1) THEN
  RETURN;
END IF;

  BEGIN
    select max(invoice_no) into v_max_invoice_no from invoice_person_lease;

  EXCEPTION

    WHEN OTHERS THEN
      v_output:=-1;
      err_code := SQLCODE;
        err_msg := SUBSTR(SQLERRM, 1, 200)||': while selecting max invoice no from invoice_person_lease';
        dbms_output.put_line('error_code: '||err_code||' error_message: '||err_msg);
        insert into error_table values('create_invoice_procedure',err_code,err_msg,sysdate);
        commit;
        return;
  END;


IF (v_max_invoice_no is NULL) THEN
	v_max_invoice_no := 1;
END IF;



  v_payment_date := v_move_in_date + v_date_increment;

  v_total_payment_due := v_living_rent+v_parking_fees+v_late_fees+v_incidentalCharges;

BEGIN
  
  SELECT count(*) into v_count from invoice_person_lease
  where person_id = v_person_id
  and lease_no = v_lease_no;
  
EXCEPTION
  WHEN OTHERS THEN
       v_output:=-1;
      err_code := SQLCODE;
        err_msg := SUBSTR(SQLERRM, 1, 200)||' while getting counting records for person and lease no in invoice_person_lease';
        dbms_output.put_line('error_code: '||err_code||' error_message: '||err_msg);
        insert into error_table values('create_invoice_procedure',err_code,err_msg,sysdate);
        commit;
        return;

END;

IF (v_count != v_input_count) THEN

  BEGIN
      INSERT INTO invoice_person_lease values(v_living_rent,
                          v_parking_fees,
                          v_late_fees,
                          v_incidentalCharges,
                          v_max_invoice_no+1,
                          v_payment_date,
                          v_mode_of_payment,
                          v_lease_no,
                          v_payment_status,
                          v_total_payment_due,
                          v_damage_charges,
                          v_person_id);

      COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      dbms_output.put_line('Insertion failed due to some reason');
        err_code := SQLCODE;
        err_msg := SUBSTR(SQLERRM, 1, 200);
        dbms_output.put_line('error_code: '||err_code||' error_message: '||err_msg);
        insert into error_table values('create_invoice_procedure',err_code,err_msg,sysdate);
        commit;
        v_output:= -1;
  END;

END IF;

END;
/
