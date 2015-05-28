CREATE OR REPLACE procedure get_best_accomodation_id_hall(v_accomodation_type IN VARCHAR2,
                      v_person_id IN NUMBER,
                      v_out_accomodation_id OUT NUMBER)
AS


/*DECLARE

v_accomodation_type VARCHAR2(200) := 'Residence Hall';
v_person_id NUMBER:= 15;
v_out_accomodation_id NUMBER;*/

v_count                   NUMBER ;
v_hall_number             residence_hall.hall_number%TYPE;

err_code                  VARCHAR2(500);
err_msg                   VARCHAR2(500);

v_is_prev_match_fail      BOOLEAN := true;


--INPUT PERSON ID'S ATTRIBUTES FOR COMPARISON

v_input_gender            VARCHAR2(100);
v_input_nationality       VARCHAR2(100);
v_input_smoker            VARCHAR2(100);
v_input_study_field       VARCHAR2(100);
v_input_dob               DATE;


--CURSOR PERSON ID'S ATTRIBUTES FOR COMPARISON

v_cur_gender            VARCHAR2(100);
v_cur_nationality       VARCHAR2(100);
v_cur_smoker            VARCHAR2(100);
v_cur_study_field       VARCHAR2(100);
v_cur_dob               DATE;


v_cur_person_id         person_accomodation_lease.person_id%TYPE;
v_cur_hall_number       residence_hall_provides_room.hall_number%TYPE;


v_max_person_id       person_accomodation_lease.person_id%TYPE;
v_max_hall_number     residence_hall_provides_room.hall_number%TYPE := 0;

v_total_count  NUMBER :=0;
v_max_count NUMBER :=0;

v_cur_residence_hall_exists boolean := false;

-- CURSOR WHICH WILL SELECT ALL PERSONS LIVING IN HALLS THAT HAVE AVAILABLE ROOMS
cursor cur_residence_hall
IS
select p.person_id,ra.hall_number,p.gender,p.nationality,p.smoker,p.study_field,p.dob
from residence_hall_provides_room ra,person_accomodation_lease pal,person p
where ra.accomodation_id = pal.accomodation_id
and pal.person_id = p.person_id
-- and where one or more rooms are empty in that hall 
and ra.hall_number in (select r1.hall_number
from residence_hall_provides_room r1 
where r1.accomodation_id not in (select accomodation_id from person_accomodation_lease pal1)
and r1.hall_number in (select hall_number
from residence_hall_provides_room rhpr, person_accomodation_lease pal2
where rhpr.accomodation_id = pal2.accomodation_id));


BEGIN
  
   
    v_out_accomodation_id := -1;  -- -1 INDICATES NO HALL EXISTS WHERE AN EMPTY ROOM IS THERE                  
   
    IF (v_accomodation_type = 'Residence Hall') THEN

    -- GET ALL THE VALUES OF COMPATIBILITY ATTRIBUTES FOR THE INPUT PERSON ID PASSED

       BEGIN

          select p.gender,p.nationality,p.smoker,p.study_field,p.dob
          into v_input_gender,v_input_nationality,v_input_smoker,v_input_study_field,v_input_dob
          from person p
          where p.person_id = v_person_id;

          EXCEPTION 
              WHEN NO_DATA_FOUND THEN 
                  v_hall_number := -1;

              WHEN OTHERS THEN
                  NULL;
                  RETURN;

          END;


          IF NOT cur_residence_hall%ISOPEN THEN

              OPEN cur_residence_hall;

          END IF;


          IF cur_residence_hall%ISOPEN THEN

              LOOP

                  FETCH cur_residence_hall into v_cur_person_id,v_cur_hall_number,v_cur_gender,v_cur_nationality,v_cur_smoker,v_cur_study_field,v_cur_dob;
                
                  EXIT WHEN cur_residence_hall%notfound;
                  -- dbms_output.put_line(c_id || ' ' || c_name || ' ' || c_addr);
                  
                  v_total_count := 0;

                  v_cur_residence_hall_exists := true;

                  If (v_cur_gender = v_input_gender) THEN
                    v_total_count := v_total_count + 1;
                  END IF;

                  If (v_cur_nationality = v_input_nationality) THEN
                    v_total_count := v_total_count + 1;
                  END IF;

                  If (v_cur_smoker = v_input_smoker) THEN
                    v_total_count := v_total_count + 1;
                  END IF;

                  If (v_cur_study_field = v_input_study_field) THEN
                    v_total_count := v_total_count + 1;
                  END IF;

                  If (ABS(v_cur_dob - v_input_dob) < 1) THEN
                    v_total_count := v_total_count + 1;
                  END IF;


                  If (v_total_count > v_max_count) THEN
                    v_max_count := v_total_count;
                    v_max_person_id := v_cur_person_id;
                    v_max_hall_number := v_cur_hall_number;
                  END IF;

              END LOOP;


              IF cur_residence_hall%ISOPEN THEN

                  CLOSE cur_residence_hall;

              END IF;


              IF v_cur_residence_hall_exists = true THEN
                -- FIND THE ROOM IN THAT HALL NUMBER WHICH IS empty

                    BEGIN

                        select accomodation_id into v_out_accomodation_id
                        from residence_hall_provides_room
                        where hall_number = v_max_hall_number
                        and accomodation_id not in (select accomodation_id from person_accomodation_lease);

                    EXCEPTION
                      WHEN OTHERS THEN
                        NULL;

                    END;

              END IF;


          END IF;   --END OF cur_residence_hall is open if condition
          
          dbms_output.put_line('OUTPUT ACCOMODATION_ID :='||v_out_accomodation_id);
          dbms_output.put_line('OUTPUT v_max_hall_number :='||v_max_hall_number);

END IF;   --// END OF ACCOMODATION TYPE = RESIDENCE HALL IF CHECK CONDITION

EXCEPTION
      WHEN OTHERS THEN
        RETURN;
END;    --//END OF OUTERMOST BEGIN END BLOCK
/