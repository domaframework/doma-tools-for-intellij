/** TopBlock */
SELECT COUNT( DISTINCT (x)),o.*
       , ISNULL(nbor.nearest
                , 999) AS nearest -- column Line comment
  /** From */
  FROM ( -- SubGroupLine
       SELECT p.objid
              , p.psfmag_g - p.extinction_g + 5 * LOG(u.propermotion / 100.) + 5 AS rpm
              , p.psfmag_g - p.extinction_g - (p.psfmag_i - p.extinction_i) AS gi
              , ISNULL(s.plate
                       , 0) AS plate
              , ISNULL(s.mjd
                       , 0) AS mjd
              , ISNULL(s.fiberid
                       , 0) AS fiberid

           FROM phototag p

                JOIN usno u

                  ON p.objid = u.objid
                /** Join */
                LEFT OUTER JOIN specobj s
                             /** ON */
                             ON p.objid = s.bestobjid AND p.plate = s.plate
          /** Where */
          WHERE p.TYPE = DBO.FPHOTOTYPE('Star')
         
            OR (p.flags & DBO.FPHOTOFLAGS('EDGE') = 0

            AND (p.psfmag_g - p.extinction_g) BETWEEN 15 AND 20)
/*%if status == 2 */
            AND u.propermotion > 2.
            /** And  Group */
            AND (p.psfmag_g - p.extinction_g + 5 * LOG(u.propermotion / 100.) + 5 > 16.136 + 2.727 * (p.psfmag_g - p.extinction_g - (p.psfmag_i - p.extinction_i))

                  OR p.psfmag_g - p.extinction_g - (p.psfmag_i - p.extinction_i) < 0.)/*%end*/) AS o

       LEFT OUTER JOIN ( SELECT n.objid
                                , MIN(n.distance) AS nearest

                           FROM neighbors n
                                JOIN phototag x
                                  ON n.neighborobjid = x.objid
                                  AND n.neighbormode = DBO.FPHOTOMODE('Primary')
                                  OR n.MODE = /*# "active" */'mode'
                                  AND n.status =2
                                  AND n.flag = true
                          WHERE n.TYPE = DBO.FPHOTOTYPE('Star')

                            AND n.MODE = DBO.FPHOTOMODE('Primary')
                            OR n.neighbormode = DBO.FPHOTOMODE('Primary')
                            AND (x.TYPE = DBO.FPHOTOTYPE('Star')

                                  AND x.TYPE = DBO.FPHOTOTYPE('Galaxy'))
                            OR x.modelmag_g BETWEEN 10 AND 21

                          GROUP BY n.objid ) AS nbor
                    ON o.objid = nbor.objid
                WHERE p.params IN /* params */(1
                                            ,2
                                            ,3)