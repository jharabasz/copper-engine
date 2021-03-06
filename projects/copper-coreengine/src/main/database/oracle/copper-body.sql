CREATE OR REPLACE PACKAGE BODY COP_COREENGINE
AS

c_STATE_ENQUEUED CONSTANT NUMBER(1) := 0;
c_STATE_WAITING  CONSTANT NUMBER(1) := 2;
c_STATE_FINISHED CONSTANT NUMBER(1) := 3;
c_STATE_INVALID  CONSTANT NUMBER(1) := 4;
c_STATE_ERROR    CONSTANT NUMBER(1) := 5;

PROCEDURE enqueue(i_MAX IN NUMBER, o_ROWCOUNT OUT NUMBER)
IS
	l_WORKFLOW_INSTANCE_ID COP_VARCHAR128_ARRAY;
	l_rowid COP_VARCHAR128_ARRAY;
	l_ppool_id COP_VARCHAR128_ARRAY;
	l_prio COP_NUMBER_ARRAY;
BEGIN
	select WORKFLOW_INSTANCE_ID, rowidtochar(wfi_rowid), ppool_id, priority bulk collect into l_WORKFLOW_INSTANCE_ID, l_rowid, l_ppool_id, l_prio 
	from (
		select WORKFLOW_INSTANCE_ID, max(is_timed_out) is_timed_out, min(wfi_rowid) wfi_rowid, min(min_numb_of_responses) min_numb_of_responses, sum(decode(rcid,NULL,0,1)) c, min(ppool_id) ppool_id, min(priority) priority 
		from (
			select case when w.timeout_ts < systimestamp then 1 else 0 end is_timed_out, r.correlation_id rcid, w.correlation_id, w.WORKFLOW_INSTANCE_ID, w.wfi_rowid, MIN_NUMB_OF_RESP min_numb_of_responses, w.ppool_id, w.priority from (select unique correlation_id from cop_response) r, cop_wait w where w.correlation_id = r.correlation_id(+) and w.state=0 and (r.correlation_id is not null or w.timeout_ts < systimestamp)
		) group by WORKFLOW_INSTANCE_ID
	) 
	where (c >= min_numb_of_responses or is_timed_out = 1) and rownum <= i_MAX;
	
	update cop_wait set state=1 where WORKFLOW_INSTANCE_ID in (select column_value from table(l_WORKFLOW_INSTANCE_ID));

	FORALL i IN 1..l_rowid.count
		INSERT INTO COP_QUEUE (PPOOL_ID, PRIORITY, LAST_MOD_TS, wfi_rowid) VALUES (l_ppool_id(i), l_prio(i), SYSTIMESTAMP, l_rowid(i));
		
	o_ROWCOUNT := l_WORKFLOW_INSTANCE_ID.count;
END;	

PROCEDURE restart(i_WORKFLOW_INSTANCE_ID IN VARCHAR2)
IS
BEGIN
	INSERT INTO COP_QUEUE (PPOOL_ID, PRIORITY, LAST_MOD_TS, wfi_rowid) (SELECT PPOOL_ID, PRIORITY, SYSTIMESTAMP, ROWID FROM COP_WORKFLOW_INSTANCE WHERE ID=i_WORKFLOW_INSTANCE_ID AND (STATE=c_STATE_ERROR OR STATE=c_STATE_INVALID));
	IF SQL%ROWCOUNT > 0 THEN
		UPDATE COP_WORKFLOW_INSTANCE SET STATE=c_STATE_ENQUEUED WHERE ID=i_WORKFLOW_INSTANCE_ID AND (STATE=c_STATE_ERROR OR STATE=c_STATE_INVALID);
	END IF;
END;

PROCEDURE restart_all
IS
	l_WORKFLOW_INSTANCE_ID COP_VARCHAR128_ARRAY;
	l_NOW TIMESTAMP;
BEGIN
	l_NOW := SYSTIMESTAMP;
	LOOP
		SELECT ID BULK COLLECT INTO l_WORKFLOW_INSTANCE_ID FROM COP_WORKFLOW_INSTANCE WHERE STATE=c_STATE_ERROR OR STATE=c_STATE_INVALID AND LAST_MOD_TS<=l_NOW AND ROWNUM <= 10000;
		IF l_WORKFLOW_INSTANCE_ID.count <> 0 THEN
			FORALL i IN 1..l_WORKFLOW_INSTANCE_ID.count
				INSERT INTO COP_QUEUE (PPOOL_ID, PRIORITY, LAST_MOD_TS, wfi_rowid) (SELECT PPOOL_ID, PRIORITY, SYSTIMESTAMP, ROWID FROM COP_WORKFLOW_INSTANCE WHERE ID=l_WORKFLOW_INSTANCE_ID(i) AND (STATE=c_STATE_ERROR OR STATE=c_STATE_INVALID));
			FORALL i IN 1..l_WORKFLOW_INSTANCE_ID.count
				UPDATE COP_WORKFLOW_INSTANCE SET STATE=c_STATE_ENQUEUED WHERE ID=l_WORKFLOW_INSTANCE_ID(i) AND (STATE=c_STATE_ERROR OR STATE=c_STATE_INVALID);
			COMMIT;
		ELSE
			EXIT;
		END IF;
	END LOOP;
END;

PROCEDURE trigger_failover(i_FAILED_ENGINE_ID IN VARCHAR)
IS
BEGIN
	UPDATE COP_QUEUE SET ENGINE_ID=NULL WHERE ENGINE_ID=i_FAILED_ENGINE_ID;
END;

/*
PROCEDURE do_heartbeat(i_ENGINE_ID IN VARCHAR)
IS
BEGIN
	UPDATE COP_ENGINE SET LAST_HEARTBEAT_TS=SYSTIMESTAMP, NEXT_HEARTBEAT_TS=(SYSTIMESTAMP + interval '1' minute) WHERE ENGINE_ID=i_ENGINE_ID;
	IF SQL%ROWCOUNT = 0 THEN
		INSERT INTO 
	END IF;
END;
*/

END;
/
show errors;

