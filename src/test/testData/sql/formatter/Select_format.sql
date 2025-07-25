/** TopBlock */
SELECT COUNT(DISTINCT x) AS count_x
       , o.*
       , COALESCE(nbor.nearest
                  , 999) AS nearest
  /** From */
  FROM ( -- SubGroupLine
         SELECT p.objid
                , p.psfmag_g - p.extinction_g + 5 * LOG(10
                                                        , u.propermotion / 100.0) + 5 AS rpm
                , p.psfmag_g - p.extinction_g - (p.psfmag_i - p.extinction_i) AS gi
                , COALESCE(s.plate
                           , 0) AS plate
                , COALESCE(s.mjd
                           , 0) AS mjd
                , COALESCE(s.fiberid
                           , 0) AS fiberid
           FROM phototag p
                -- Line1
                JOIN usno u
                  -- Line2
                  ON p.objid = u.objid
                /** Join */
                LEFT OUTER JOIN specobj s
                             /** ON */
                             ON p.objid = s.bestobjid
                            AND p.plate = s.plate
          /** Where */
          WHERE p.TYPE = 'Star'
             -- Line3
             OR (p.flags & FPHOTOFLAGS('EDGE') = 0
                 AND (p.psfmag_g - p.extinction_g) BETWEEN 15 AND 20)
            /*%if status == 2 */
            -- Line4
            AND u.propermotion > 2.0
            /** And  Group */
            AND (p.psfmag_g - p.extinction_g + 5 * LOG(10
                                                       , u.propermotion / 100.0) + 5 > 16.136 + 2.727
                  OR (p.psfmag_g - p.extinction_g - (p.psfmag_i - p.extinction_i)) < 0.0
                 AND (p.extinction_g - u.propermotion) > 0)
            /*%end*/ ) AS o
       LEFT OUTER JOIN ( SELECT n.objid
                                , MIN(n.distance) AS nearest
                           FROM neighbors n
                                JOIN phototag x
                                  ON n.neighborobjid = x.objid
                                 AND (n.neighbormode = 'Primary'
                                       OR n.mode = 'mode')
                                 AND n.status = 2
                                 AND n.flag = TRUE
                          WHERE n.TYPE = 'Star'
                             OR (n.MODE = 'Primary'
                                  OR n.neighbormode = 'Primary')
                            AND ((x.TYPE = 'Star'
                                  AND x.TYPE = 'Galaxy')
                                  OR x.modelmag_g BETWEEN 10 AND 21)
                          GROUP BY n.objid ) AS nbor
                    ON o.objid = nbor.objid
 WHERE o.objid IN /* params */(1, 2, 3)
